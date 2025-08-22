package com.todoapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static ConfigurationManager instance;
    private final Properties properties;

    private ConfigurationManager() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("application.properties file not found, using defaults");
                setDefaultProperties();
                return;
            }
            properties.load(input);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/todoapp");
        properties.setProperty("db.username", "todouser");
        properties.setProperty("db.password", "todopass");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("file.storage.path", "data/tasks.json");
        properties.setProperty("file.storage.backup.path", "data/tasks_backup.json");
        properties.setProperty("app.title", "Todo List Manager");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("app.default.window.width", "800");
        properties.setProperty("app.default.window.height", "600");
        properties.setProperty("logging.level", "INFO");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}: {}", key, properties.getProperty(key));
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}
