package com.todoapp.controller;

import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.service.TaskService;
import com.todoapp.service.TaskService.TaskFilter;
import com.todoapp.service.TaskService.TaskSortCriteria;
import com.todoapp.service.TaskService.TaskStatistics;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // FXML injected components
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Long> idColumn;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> descriptionColumn;
    @FXML private TableColumn<Task, Priority> priorityColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> createdDateColumn;

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Priority> priorityComboBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField dueTimeField;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<TaskFilter> filterComboBox;
    @FXML private ComboBox<TaskSortCriteria> sortComboBox;
    @FXML private CheckBox ascendingCheckBox;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button completeButton;
    @FXML private Button clearCompletedButton;
    
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label overdueTasksLabel;
    @FXML private ProgressBar completionProgressBar;
    
    @FXML private Label storageTypeLabel;

    // Service and data
    private TaskService taskService;
    private ObservableList<Task> taskList;
    private Task selectedTask;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeService();
        initializeTableView();
        initializeControls();
        initializeEventHandlers();
        refreshTaskList();
        updateStatistics();
    }

    private void initializeService() {
        taskService = new TaskService();
        taskList = FXCollections.observableArrayList();
        
        // Update storage type label
        Platform.runLater(() -> {
            storageTypeLabel.setText("Storage: " + 
                (taskService.isUsingDatabaseStorage() ? "Database" : "File"));
        });
    }

    private void initializeTableView() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        dueDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dueDate = cellData.getValue().getDueDate();
            return new SimpleStringProperty(dueDate != null ? dueDate.format(DATE_FORMATTER) : "");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            String status = task.isCompleted() ? "Completed" : "Pending";
            if (!task.isCompleted() && task.isOverdue()) {
                status += " (Overdue)";
            }
            return new SimpleStringProperty(status);
        });
        
        createdDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime createdDate = cellData.getValue().getCreatedDate();
            return new SimpleStringProperty(createdDate != null ? createdDate.format(DATE_FORMATTER) : "");
        });

        // Set cell factories for styling
        priorityColumn.setCellFactory(column -> new TableCell<Task, Priority>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority.getDisplayName());
                    setStyle("-fx-background-color: " + priority.getColor() + "40;");
                }
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.contains("Overdue")) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (status.equals("Completed")) {
                        setStyle("-fx-text-fill: #28a745;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        taskTable.setItems(taskList);
        
        // Selection listener
        taskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedTask = newSelection;
            updateFormFields();
            updateButtonStates();
        });
    }

    private void initializeControls() {
        // Initialize combo boxes
        priorityComboBox.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityComboBox.setValue(Priority.MEDIUM);
        
        filterComboBox.setItems(FXCollections.observableArrayList(TaskFilter.values()));
        filterComboBox.setValue(TaskFilter.ALL);
        
        sortComboBox.setItems(FXCollections.observableArrayList(TaskSortCriteria.values()));
        sortComboBox.setValue(TaskSortCriteria.CREATED_DATE);
        
        ascendingCheckBox.setSelected(false);
        
        // Set time field placeholder
        dueTimeField.setPromptText("HH:MM (24-hour format)");
        
        updateButtonStates();
    }

    private void initializeEventHandlers() {
        // Button event handlers
        addButton.setOnAction(e -> addTask());
        updateButton.setOnAction(e -> updateTask());
        deleteButton.setOnAction(e -> deleteTask());
        completeButton.setOnAction(e -> toggleTaskCompletion());
        clearCompletedButton.setOnAction(e -> clearCompletedTasks());
        
        // Search and filter handlers
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFiltersAndSort());
        filterComboBox.setOnAction(e -> applyFiltersAndSort());
        sortComboBox.setOnAction(e -> applyFiltersAndSort());
        ascendingCheckBox.setOnAction(e -> applyFiltersAndSort());
        
        // Form validation
        titleField.textProperty().addListener((obs, oldText, newText) -> updateButtonStates());
    }

    @FXML
    private void addTask() {
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            Priority priority = priorityComboBox.getValue();
            LocalDateTime dueDate = parseDueDateTime();

            Task task = taskService.createTask(title, description, priority, dueDate);
            logger.info("Task created: {}", task.getTitle());
            
            clearForm();
            refreshTaskList();
            updateStatistics();
            
            showSuccess("Task created successfully!");
            
        } catch (Exception e) {
            logger.error("Error creating task", e);
            showError("Error creating task: " + e.getMessage());
        }
    }

    @FXML
    private void updateTask() {
        if (selectedTask == null) return;
        
        try {
            Task updatedTask = selectedTask.copy();
            updatedTask.setTitle(titleField.getText().trim());
            updatedTask.setDescription(descriptionArea.getText().trim());
            updatedTask.setPriority(priorityComboBox.getValue());
            updatedTask.setDueDate(parseDueDateTime());

            taskService.updateTask(updatedTask);
            logger.info("Task updated: {}", updatedTask.getTitle());
            
            clearForm();
            refreshTaskList();
            updateStatistics();
            
            showSuccess("Task updated successfully!");
            
        } catch (Exception e) {
            logger.error("Error updating task", e);
            showError("Error updating task: " + e.getMessage());
        }
    }

    @FXML
    private void deleteTask() {
        if (selectedTask == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText("Task: " + selectedTask.getTitle());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(selectedTask.getId());
                logger.info("Task deleted: {}", selectedTask.getTitle());
                
                clearForm();
                refreshTaskList();
                updateStatistics();
                
                showSuccess("Task deleted successfully!");
                
            } catch (Exception e) {
                logger.error("Error deleting task", e);
                showError("Error deleting task: " + e.getMessage());
            }
        }
    }

    @FXML
    private void toggleTaskCompletion() {
        if (selectedTask == null) return;
        
        try {
            if (selectedTask.isCompleted()) {
                taskService.markTaskPending(selectedTask.getId());
                logger.info("Task marked as pending: {}", selectedTask.getTitle());
            } else {
                taskService.markTaskCompleted(selectedTask.getId());
                logger.info("Task marked as completed: {}", selectedTask.getTitle());
            }
            
            refreshTaskList();
            updateStatistics();
            
        } catch (Exception e) {
            logger.error("Error toggling task completion", e);
            showError("Error updating task: " + e.getMessage());
        }
    }

    @FXML
    private void clearCompletedTasks() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Completed Tasks");
        alert.setHeaderText("Are you sure you want to delete all completed tasks?");
        alert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int deletedCount = taskService.deleteAllCompletedTasks();
                logger.info("Deleted {} completed tasks", deletedCount);
                
                refreshTaskList();
                updateStatistics();
                
                showSuccess(deletedCount + " completed tasks deleted!");
                
            } catch (Exception e) {
                logger.error("Error clearing completed tasks", e);
                showError("Error clearing completed tasks: " + e.getMessage());
            }
        }
    }

    private void applyFiltersAndSort() {
        try {
            List<Task> tasks;
            
            // Apply search filter
            String searchTerm = searchField.getText();
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                tasks = taskService.searchTasks(searchTerm);
            } else {
                // Apply selected filter
                TaskFilter filter = filterComboBox.getValue();
                tasks = taskService.getFilteredTasks(filter);
            }
            
            // Apply sorting
            TaskSortCriteria sortCriteria = sortComboBox.getValue();
            boolean ascending = ascendingCheckBox.isSelected();
            
            if (sortCriteria != null) {
                tasks = taskService.getTasksSortedBy(sortCriteria, ascending);
                
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    final String finalSearchTerm = searchTerm.toLowerCase();
                    tasks = tasks.stream()
                            .filter(task -> 
                                (task.getTitle() != null && task.getTitle().toLowerCase().contains(finalSearchTerm)) ||
                                (task.getDescription() != null && task.getDescription().toLowerCase().contains(finalSearchTerm)))
                            .collect(Collectors.toList());
                }
                
                // Apply filter to sorted and searched results
                TaskFilter filter = filterComboBox.getValue();
                if (filter != TaskFilter.ALL && (searchTerm == null || searchTerm.trim().isEmpty())) {
                    // Only apply filter if no search term (to avoid double filtering)
                    tasks = tasks.stream()
                            .filter(task -> matchesFilter(task, filter))
                            .collect(Collectors.toList());
                }
            }
            
            taskList.setAll(tasks);
            
        } catch (Exception e) {
            logger.error("Error applying filters and sort", e);
            showError("Error filtering tasks: " + e.getMessage());
        }
    }

    private boolean matchesFilter(Task task, TaskFilter filter) {
        switch (filter) {
            case ALL:
                return true;
            case PENDING:
                return !task.isCompleted();
            case COMPLETED:
                return task.isCompleted();
            case OVERDUE:
                return task.isOverdue();
            case DUE_TODAY:
                return task.isDueToday();
            case HIGH_PRIORITY:
                return task.getPriority() == Priority.HIGH;
            case URGENT:
                return task.getPriority() == Priority.URGENT;
            default:
                return true;
        }
    }

    private void refreshTaskList() {
        applyFiltersAndSort();
    }

    private void updateFormFields() {
        if (selectedTask == null) {
            clearForm();
            return;
        }
        
        titleField.setText(selectedTask.getTitle());
        descriptionArea.setText(selectedTask.getDescription());
        priorityComboBox.setValue(selectedTask.getPriority());
        
        if (selectedTask.getDueDate() != null) {
            dueDatePicker.setValue(selectedTask.getDueDate().toLocalDate());
            dueTimeField.setText(selectedTask.getDueDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            dueDatePicker.setValue(null);
            dueTimeField.setText("");
        }
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        priorityComboBox.setValue(Priority.MEDIUM);
        dueDatePicker.setValue(null);
        dueTimeField.clear();
        selectedTask = null;
        taskTable.getSelectionModel().clearSelection();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedTask != null;
        boolean hasTitle = titleField.getText() != null && !titleField.getText().trim().isEmpty();
        
        addButton.setDisable(!hasTitle || hasSelection);
        updateButton.setDisable(!hasTitle || !hasSelection);
        deleteButton.setDisable(!hasSelection);
        completeButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            completeButton.setText(selectedTask.isCompleted() ? "Mark Pending" : "Mark Complete");
        } else {
            completeButton.setText("Mark Complete");
        }
    }

    private void updateStatistics() {
        try {
            TaskStatistics stats = taskService.getStatistics();
            
            totalTasksLabel.setText("Total: " + stats.getTotal());
            completedTasksLabel.setText("Completed: " + stats.getCompleted());
            pendingTasksLabel.setText("Pending: " + stats.getPending());
            overdueTasksLabel.setText("Overdue: " + stats.getOverdue());
            
            completionProgressBar.setProgress(stats.getCompletionPercentage() / 100.0);
            
        } catch (Exception e) {
            logger.error("Error updating statistics", e);
        }
    }

    private LocalDateTime parseDueDateTime() {
        if (dueDatePicker.getValue() == null) {
            return null;
        }
        
        String timeText = dueTimeField.getText();
        if (timeText == null || timeText.trim().isEmpty()) {
            // Default to end of day
            return dueDatePicker.getValue().atTime(23, 59);
        }
        
        try {
            String[] timeParts = timeText.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
            
            return dueDatePicker.getValue().atTime(hour, minute);
            
        } catch (Exception e) {
            logger.warn("Invalid time format, using end of day: {}", timeText);
            return dueDatePicker.getValue().atTime(23, 59);
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
