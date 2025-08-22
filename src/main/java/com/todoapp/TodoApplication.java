package com.todoapp;

import com.todoapp.controller.MainController;
import com.todoapp.util.ConfigurationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Main JavaFX Application class
public class TodoApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(TodoApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            ConfigurationManager config = ConfigurationManager.getInstance();
            
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Get controller and set stage reference
            MainController controller = loader.getController();
            controller.setStage(primaryStage);
            
            // Configure stage
            primaryStage.setTitle(config.getProperty("app.title", "Todo List Manager"));
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            
            // Set default size
            primaryStage.setWidth(config.getIntProperty("app.default.window.width", 800));
            primaryStage.setHeight(config.getIntProperty("app.default.window.height", 600));
            
            // Add application icon (if available)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/app-icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                logger.debug("Application icon not found, using default");
            }
            
            // Show the stage
            primaryStage.show();
            
            logger.info("Todo List application started successfully");
            
        } catch (Exception e) {
            logger.error("Error starting application", e);
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        logger.info("Todo List application shutting down");
        // Cleanup resources if needed
        try {
            com.todoapp.database.DatabaseConnection.getInstance().closeConnection();
        } catch (Exception e) {
            logger.warn("Error during cleanup", e);
        }
    }

    private void showErrorAndExit(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String[] args) {
        // Set system properties for better JavaFX experience
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        logger.info("Starting Todo List application...");
        launch(args);
    }
}
