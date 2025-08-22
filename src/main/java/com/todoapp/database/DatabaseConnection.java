package com.todoapp.database;

import com.todoapp.util.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static DatabaseConnection instance;
    private Connection connection;
    
    private final String url;
    private final String username;
    private final String password;
    private final String driver;

    private DatabaseConnection() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        this.url = config.getProperty("db.url");
        this.username = config.getProperty("db.username");
        this.password = config.getProperty("db.password");
        this.driver = config.getProperty("db.driver");
        
        try {
            Class.forName(driver);
            logger.info("Database driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Database driver not found", e);
            throw new RuntimeException("Database driver not found", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(url, username, password);
                logger.info("Database connection established");
                initializeDatabase();
            } catch (SQLException e) {
                logger.error("Failed to establish database connection", e);
                throw e;
            }
        }
        return connection;
    }

    private void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            // Create tasks table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', " +
                    "completed BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "due_date TIMESTAMP NULL, " +
                    "created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "completed_date TIMESTAMP NULL, " +
                    "INDEX idx_priority (priority), " +
                    "INDEX idx_completed (completed), " +
                    "INDEX idx_due_date (due_date), " +
                    "INDEX idx_created_date (created_date)" +
                    ")";
            
            statement.executeUpdate(createTableSQL);
            logger.info("Database table 'tasks' initialized successfully");
            
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
