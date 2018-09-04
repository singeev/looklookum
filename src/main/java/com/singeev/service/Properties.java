package com.singeev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class Properties {

    private final static Logger LOGGER = LoggerFactory.getLogger(Properties.class);

    private static java.util.Properties properties;

    public static void loadProperties() {
        properties = new java.util.Properties();
        InputStream inputStream = Properties.class
                .getClassLoader()
                .getResourceAsStream("app.properties");
        try {
            properties.load(inputStream);
            inputStream.close();
            LOGGER.info("Application properties loaded successfully");
        } catch (IOException e) {
            LOGGER.error("Can't load application properties: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
