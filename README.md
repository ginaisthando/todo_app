# Java To-Do List Application

![Java](https://img.shields.io/badge/Java-11+-red?logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/Database-MySQL-orange?logo=mysql&logoColor=white)
![Status](https://img.shields.io/badge/Status-Active-success)
![License](https://img.shields.io/badge/License-MIT-green)

![Repo Size](https://img.shields.io/github/repo-size/ginaisthando/todo_app)
![Last Commit](https://img.shields.io/github/last-commit/ginaisthando/todo_app)
![Open Issues](https://img.shields.io/github/issues/ginaisthando/todo_app)
![Stars](https://img.shields.io/github/stars/ginaisthando/todo_app?style=social)

A comprehensive **desktop application** for managing tasks efficiently, built with **Java, JavaFX, and MySQL/File storage**.  

---

## Features

### Core Functionality
- **CRUD Operations**: Create, Read, Update, and Delete tasks
- **Task Completion**: Mark tasks as complete or pending
- **Priority Levels**: Assign priority levels (Low, Medium, High, Urgent) with color coding
- **Due Dates**: Set and track due dates with time support
- **Task Descriptions**: Add detailed descriptions to tasks

### Advanced Features
- **Search & Filter**: Search tasks by title/description and filter by status, priority, or due date
- **Sorting**: Sort tasks by title, priority, due date, created date, or completion date
- **Statistics Dashboard**: View completion progress, overdue tasks, and task counts
- **Dual Storage**: Automatic fallback from MySQL database to JSON file storage
- **Responsive GUI**: Modern JavaFX interface with intuitive controls

### Technical Highlights
- **Object-Oriented Design**: Demonstrates proper OOP principles with encapsulation and inheritance
- **Design Patterns**: Implements Singleton, DAO, and MVC patterns
- **Exception Handling**: Comprehensive error handling and logging
- **Collections Framework**: Extensive use of Java collections and streams
- **Database Integration**: MySQL database with connection pooling and prepared statements
- **File I/O**: JSON-based file storage with backup capabilities

## Architecture

### Project Structure
```
src/main/java/com/todoapp/
├── model/                  # Data models
│   ├── Task.java          # Task entity with business logic
│   └── Priority.java      # Priority enumeration
├── dao/                   # Data Access Objects
│   ├── TaskDAO.java       # DAO interface
│   ├── TaskDAOImpl.java   # MySQL implementation
│   └── FileTaskDAO.java   # File-based implementation
├── database/              # Database connection management
│   └── DatabaseConnection.java
├── service/               # Business logic layer
│   └── TaskService.java   # Task operations and filtering
├── controller/            # JavaFX controllers
│   └── MainController.java
├── util/                  # Utility classes
│   └── ConfigurationManager.java
└── TodoApplication.java   # Main application class

src/main/resources/
├── fxml/                  # JavaFX FXML layouts
│   └── main.fxml
├── styles/                # CSS stylesheets
│   └── application.css
└── application.properties # Configuration file
```

### Design Patterns Used
1. **Singleton Pattern**: DatabaseConnection, ConfigurationManager
2. **Data Access Object (DAO)**: TaskDAO interface with multiple implementations
3. **Model-View-Controller (MVC)**: Separation of concerns in GUI architecture
4. **Strategy Pattern**: Multiple storage strategies (Database vs File)

## Prerequisites

### Software Requirements
- Java 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher (optional - file storage available as fallback)

### Development Environment
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code)
- JavaFX SDK (included in dependencies)

## Setup Instructions

### 1. Database Setup (Optional)
If you want to use MySQL database storage:

```sql
-- Create database
CREATE DATABASE todoapp;

-- Create user
CREATE USER 'todouser'@'localhost' IDENTIFIED BY 'todopass';
GRANT ALL PRIVILEGES ON todoapp.* TO 'todouser'@'localhost';
FLUSH PRIVILEGES;
```

The application will automatically create the required tables on first run.

### 2. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd todo_app

# Build the project
mvn clean compile

# Run the application
mvn javafx:run
```

### 3. Alternative Build Methods

#### Create executable JAR:
```bash
mvn clean package
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/todo-list-app-1.0-SNAPSHOT-shaded.jar
```

#### Run with Maven:
```bash
mvn clean javafx:run
```

### 4. Configuration

Edit `src/main/resources/application.properties` to customize:

```properties
# Database Configuration (optional)
db.url=jdbc:mysql://localhost:3306/todoapp
db.username=todouser
db.password=todopass

# File Storage Configuration
file.storage.path=data/tasks.json
file.storage.backup.path=data/tasks_backup.json

# Application Configuration
app.title=Todo List Manager
app.default.window.width=800
app.default.window.height=600
```

## Usage Guide

### Basic Operations

1. **Adding Tasks**:
   - Fill in the task title (required)
   - Add description, priority, and due date
   - Click "Add Task"

2. **Editing Tasks**:
   - Select a task from the table
   - Modify fields in the form
   - Click "Update Task"

3. **Completing Tasks**:
   - Select a task and click "Mark Complete"
   - Or click "Mark Pending" to revert

4. **Deleting Tasks**:
   - Select a task and click "Delete Task"
   - Confirm the deletion

### Advanced Features

1. **Search**: Type in the search box to find tasks by title or description
2. **Filtering**: Use the filter dropdown to show specific task types
3. **Sorting**: Choose sort criteria and order (ascending/descending)
4. **Bulk Operations**: Clear all completed tasks at once

### Storage Modes

The application automatically detects available storage:
- **Database Mode**: If MySQL is configured and accessible
- **File Mode**: Falls back to JSON file storage if database is unavailable

## Technical Implementation

### Object-Oriented Programming Features

1. **Encapsulation**:
   - Private fields with public getters/setters
   - Data validation in model classes
   - Configuration management

2. **Inheritance**:
   - Exception hierarchy
   - JavaFX controller inheritance

3. **Polymorphism**:
   - DAO interface with multiple implementations
   - Strategy pattern for storage methods

4. **Abstraction**:
   - Service layer abstracts business logic
   - DAO pattern abstracts data access

### Collections Usage

1. **List Operations**:
   - ArrayList for task storage
   - ObservableList for JavaFX table binding

2. **Stream Processing**:
   - Filtering and searching tasks
   - Statistical calculations
   - Sorting operations

3. **Map Usage**:
   - Configuration properties
   - Caching mechanisms

### Exception Handling

1. **Database Exceptions**:
   - SQLException handling with proper logging
   - Automatic fallback to file storage

2. **File I/O Exceptions**:
   - IOException handling for file operations
   - Backup file recovery

3. **Validation Exceptions**:
   - Input validation with user-friendly messages
   - Business rule enforcement

## Performance Considerations

1. **Database Optimization**:
   - Prepared statements prevent SQL injection
   - Indexed columns for faster queries
   - Connection reuse

2. **Memory Management**:
   - Lazy loading of large datasets
   - Efficient collections usage
   - Proper resource cleanup

3. **UI Responsiveness**:
   - Background task processing
   - Progressive loading
   - Efficient table updates

## Troubleshooting

### Common Issues

1. **Database Connection Failed**:
   - Verify MySQL is running
   - Check connection credentials
   - Application will fallback to file storage

2. **JavaFX Runtime Error**:
   - Ensure JavaFX is in module path
   - Use correct JVM arguments
   - Check Java version compatibility

3. **File Permission Error**:
   - Ensure write permissions in data directory
   - Check file system space
   - Verify backup file accessibility

### Logging

Application logs are output to console. Set logging level in `application.properties`:
```properties
logging.level=DEBUG  # Options: DEBUG, INFO, WARN, ERROR
```
