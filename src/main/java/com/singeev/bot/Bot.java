package com.singeev.bot;

import com.singeev.entity.DetectedFace;
import com.singeev.service.CognitiveService;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import sun.java2d.SunGraphics2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Bot extends TelegramLongPollingBot {

    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private final static String TEMP_FILE_NAME = "tempFile";

    private CognitiveService cognitiveService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String response;
            long chatId = update.getMessage().getChatId();
            LOGGER.info("User {} sent message: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText());
            if (update.getMessage().getText().equals("/start")) {
                response = EmojiParser.parseToUnicode("Hi there :wave:\n" +
                        "I can look at a photo, find a person there and recognize their gender, age, and emotions.\n" +
                        "Send me your selfie or someone's photo!");
            } else {
                response = "I don't understand text. Send me a photo!";
            }
            SendMessage message = new SendMessage()
                    .setChatId(chatId)
                    .setText(response);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            long chatId = update.getMessage().getChatId();
            cognitiveService = new CognitiveService();
            List<PhotoSize> photos = update.getMessage().getPhoto();

            PhotoSize biggestPhoto = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            String filePath = null;
            File telegramFile = null;

            if(biggestPhoto != null) {
                if (biggestPhoto.hasFilePath()) { // If the file_path is already present, we are done!
                    filePath = biggestPhoto.getFilePath();
                } else { // If not, let find it
                    // We create a GetFile method and set the file_id from the photo
                    GetFile getFileMethod = new GetFile();
                    getFileMethod.setFileId(biggestPhoto.getFileId());
                    try {
                        // We execute the method using AbsSender::execute method.
                        telegramFile = execute(getFileMethod);
                        // We now have the file_path
                        filePath = telegramFile.getFilePath();
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }

            String url = File.getFileUrl(getBotToken(), filePath);
            LOGGER.info("Got photo: {}", url);
            List<DetectedFace> faces = cognitiveService.lookAtThePhoto(url);
            if(!faces.isEmpty()) {
                java.io.File photo = downloadImageFromTelegram(telegramFile);
                BufferedImage bufferedImage = convertFileToImage(photo);
                markFacesOnImageAndSaveIt(bufferedImage, faces, chatId);
                java.io.File markedPhoto = new java.io.File(TEMP_FILE_NAME + "_" + chatId + ".jpg");

                SendPhoto sendPhotoRequest = new SendPhoto();
                // Set destination chat id
                sendPhotoRequest.setChatId(chatId);
                // Set the photo file as a new photo (You can also use InputStream with a method overload)
                sendPhotoRequest.setPhoto(markedPhoto);
                try {
                    // Execute the method
                    execute(sendPhotoRequest);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                for (DetectedFace face : faces) {
                    SendMessage message = new SendMessage()
                            .setChatId(chatId)
                            .setText(face.toString());
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        LOGGER.error("Can't send message: {}", e.getMessage());
                        e.printStackTrace();
                    }
                }
                markedPhoto.delete();
            } else {
                SendMessage message = new SendMessage()
                        .setChatId(chatId)
                        .setText(EmojiParser.parseToUnicode(
                                "I can't find human faces on that picture :disappointed_relieved:"
                        ));
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    LOGGER.error("Can't send message: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            LOGGER.info("Got empty message from user {}", update.getMessage().getFrom().getId());
        }
    }

    @Override
    public String getBotUsername() {
        return "LookLookum";
    }

    @Override
    public String getBotToken() {
        return "467704002:AAH_Hit7RUivNy4OhBHo11lGhpJ3FKL2nEI";
    }

    private void markFacesOnImageAndSaveIt(BufferedImage bufferedImage, List<DetectedFace> faces, Long chatId){
        Graphics2D graphics2D = bufferedImage.createGraphics();
        float lineWidth = ((SunGraphics2D) graphics2D).getCompClip().getHiX() * 0.01f;
        for(DetectedFace face: faces) {
            DetectedFace.FaceRectangle rectangle = face.getFaceRectangle();
            graphics2D.setColor(Color.GREEN);
            graphics2D.setStroke(new BasicStroke(lineWidth));
            graphics2D.drawRect(rectangle.getLeft(), rectangle.getTop(), rectangle.getWidth(), rectangle.getHeight());
        }
        graphics2D.dispose();

        java.io.File file = new java.io.File(TEMP_FILE_NAME + "_" + chatId + ".jpg");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            ImageIO.write(bufferedImage, "jpg", file);
        } catch (IOException e) {
            LOGGER.error("Can't save temp file to disk: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private java.io.File downloadImageFromTelegram(File telegramFile) {
        try {
            return downloadFile(telegramFile);
        } catch (TelegramApiException e) {
            LOGGER.error("Can't download photo from Telegram: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private BufferedImage convertFileToImage(java.io.File photo) {
        BufferedImage img;
        try {
            img = ImageIO.read(photo);
        } catch (IOException e) {
            LOGGER.error("Can't convert photo from Telegram to java image: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        int scaleX = img.getWidth();
        int scaleY = img.getHeight();
        Image image = img.getScaledInstance(scaleX, scaleY, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(scaleX, scaleY, TYPE_INT_RGB);
        buffered.getGraphics().drawImage(image, 0, 0 , null);
        return buffered;
    }
}
