package com.singeev;

import com.singeev.bot.Bot;
import com.singeev.service.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class App {

    private final static Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        Properties.loadProperties();
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(new Bot());
            LOGGER.info("Bot started and registered successfully!");
        } catch (TelegramApiException e) {
            LOGGER.error("Can't start bot: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
