package com.singeev.bot;

import com.singeev.entity.DetectedFace;
import com.singeev.service.CognitiveService;
import com.singeev.service.ImageProcessingService;
import com.singeev.service.Properties;
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

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    @Override
    public String getBotUsername() {
        return Properties.getProperty("botUserName");
    }

    @Override
    public String getBotToken() {
        return Properties.getProperty("botToken");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            LOGGER.info("User [id={}, firstName={}] sent message: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    update.getMessage().getText());
            String response = update.getMessage().getText().equals("/start")
                ? EmojiParser.parseToUnicode("Hi there :wave:\n" +
                        "I can look at a photo, find a person there and recognize their gender, age, and emotions.\n" +
                        "Send me your selfie or someone's photo!")
                : "I don't understand text. Send me a photo!";
            sendTextMessage(update, response);
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            CognitiveService cognitiveService = new CognitiveService();
            PhotoSize biggestPhoto = update.getMessage().getPhoto()
                    .stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);

            String filePath = null;
            File telegramFile = null;
            if(biggestPhoto != null) {
                if (biggestPhoto.hasFilePath()) {
                    filePath = biggestPhoto.getFilePath();
                } else {
                    GetFile getFileMethod = new GetFile();
                    getFileMethod.setFileId(biggestPhoto.getFileId());
                    try {
                        telegramFile = execute(getFileMethod);
                        filePath = telegramFile.getFilePath();
                    } catch (TelegramApiException e) {
                        LOGGER.error("Can't download photo from Telegram: {}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            String url = File.getFileUrl(getBotToken(), filePath);
            LOGGER.info("User [id={}, firstName={}] sent photo: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    url);
            List<DetectedFace> faces = cognitiveService.lookAtThePhoto(url);
            if(!faces.isEmpty()) {
                LOGGER.info("Found {} face(s) on the photo", faces.size());
                String tempFileName = "tempImage_" + update.getMessage().getChatId() + ".jpg";
                ImageProcessingService imageProcessingService = new ImageProcessingService();
                java.io.File photoToProcess = downloadImageFromTelegram(telegramFile);
                BufferedImage bufferedImage = imageProcessingService.convertFileToImage(photoToProcess);
                imageProcessingService.markFacesOnImageAndSaveIt(bufferedImage, faces, tempFileName);
                java.io.File markedPhoto = new java.io.File(tempFileName);
                sendMessageWithPhoto(update, markedPhoto);
                faces.forEach(face -> sendTextMessage(update, face.toString()));
                markedPhoto.delete();
            } else {
                sendTextMessage(update, EmojiParser.parseToUnicode("I can't find human faces on that picture " +
                        ":disappointed_relieved:"));
            }
        } else {
            LOGGER.info("Got empty message from user [id={}, firstName={}]",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName());
        }
    }

    private void sendTextMessage(Update update, String text) {
        SendMessage message = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Can't send text message to user [id={}, firstName={}]: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessageWithPhoto(Update update, java.io.File photo) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(update.getMessage().getChatId());
        sendPhotoRequest.setPhoto(photo);
        try {
            execute(sendPhotoRequest);
            LOGGER.info("Photo with recognized faces successfully sent to user [id={}, firstName={}]",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName());
        } catch (TelegramApiException e) {
            LOGGER.error("Can't send text message to user [id={}, firstName={}]: {}",
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    e.getMessage());
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
}
