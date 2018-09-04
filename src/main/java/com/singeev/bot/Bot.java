package com.singeev.bot;

import com.singeev.entity.DetectedFace;
import com.singeev.service.CognitiveService;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private final static Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    private CognitiveService cognitiveService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();
            LOGGER.info("User {} sent message: {}", update.getMessage().getFrom().getId(), messageText);
            if (messageText.equals("/start")) {
                messageText = EmojiParser.parseToUnicode("Hi there :wave:\n" +
                        "I can look at a photo, find a person there and recognize their gender, age, and emotions.\n" +
                        "Send me your selfie or someone's photo!");
            }
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(messageText);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            long chat_id = update.getMessage().getChatId();
            cognitiveService = new CognitiveService();
            List<PhotoSize> photos = update.getMessage().getPhoto();
            String filePath = getPhotoUrl(photos);
            String url = File.getFileUrl(getBotToken(), filePath);
            LOGGER.info("Got photo: {}", url);
            List<DetectedFace> faces = cognitiveService.lookAtThePhoto(url);
            for(DetectedFace face: faces) {
                SendMessage message = new SendMessage()
                        .setChatId(chat_id)
                        .setText(face.toString());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
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

    private String getPhotoUrl(List<PhotoSize> photos) {
        PhotoSize photo = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(null);

        if(photo != null) {
            if (photo.hasFilePath()) { // If the file_path is already present, we are done!
                return photo.getFilePath();
            } else { // If not, let find it
                // We create a GetFile method and set the file_id from the photo
                GetFile getFileMethod = new GetFile();
                getFileMethod.setFileId(photo.getFileId());
                try {
                    // We execute the method using AbsSender::execute method.
                    File file = execute(getFileMethod);
                    // We now have the file_path
                    return file.getFilePath();
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
