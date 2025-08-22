package com.todoapp.service;

import com.todoapp.dao.TaskDAO;
import com.todoapp.dao.TaskDAOImpl;
import com.todoapp.dao.FileTaskDAO;
import com.todoapp.database.DatabaseConnection;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskDAO taskDAO;
    private final boolean useDatabaseStorage;

    public TaskService() {
        // Try to use database first, fallback to file storage
        this.useDatabaseStorage = initializeDatabaseConnection();
        this.taskDAO = useDatabaseStorage ? new TaskDAOImpl() : new FileTaskDAO();
        
        logger.info("TaskService initialized with {} storage", 
                   useDatabaseStorage ? "database" : "file");
    }

    private boolean initializeDatabaseConnection() {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            return dbConnection.testConnection();
        } catch (Exception e) {
            logger.warn("Database connection failed, falling back to file storage", e);
            return false;
        }
    }

    public Task createTask(String title, String description, Priority priority, LocalDateTime dueDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        Task task = new Task(title.trim(), description != null ? description.trim() : null);
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setDueDate(dueDate);

        return taskDAO.save(task);
    }

    public Task updateTask(Task task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("Invalid task for update");
        }

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        return taskDAO.save(task);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskDAO.findById(id);
    }

    public List<Task> getAllTasks() {
        return taskDAO.findAll();
    }

    public List<Task> getTasksSortedBy(TaskSortCriteria criteria, boolean ascending) {
        List<Task> tasks = taskDAO.findAll();
        
        Comparator<Task> comparator = getComparator(criteria);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        return tasks.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Comparator<Task> getComparator(TaskSortCriteria criteria) {
        switch (criteria) {
            case TITLE:
                return Comparator.comparing(Task::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
            case PRIORITY:
                return Comparator.comparing(task -> task.getPriority().getLevel());
            case DUE_DATE:
                return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case CREATED_DATE:
                return Comparator.comparing(Task::getCreatedDate);
            case COMPLETED_DATE:
                return Comparator.comparing(Task::getCompletedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(Task::getCreatedDate);
        }
    }

    public List<Task> getFilteredTasks(TaskFilter filter) {
        switch (filter) {
            case ALL:
                return taskDAO.findAll();
            case PENDING:
                return taskDAO.findByCompleted(false);
            case COMPLETED:
                return taskDAO.findByCompleted(true);
            case OVERDUE:
                return taskDAO.findOverdueTasks();
            case DUE_TODAY:
                return taskDAO.findTasksDueToday();
            case HIGH_PRIORITY:
                return taskDAO.findByPriority(Priority.HIGH);
            case URGENT:
                return taskDAO.findByPriority(Priority.URGENT);
            default:
                return taskDAO.findAll();
        }
    }

    public List<Task> searchTasks(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTasks();
        }
        return taskDAO.searchTasks(searchTerm.trim());
    }

    public Task markTaskCompleted(Long taskId) {
        Optional<Task> taskOpt = taskDAO.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setCompleted(true);
            return taskDAO.save(task);
        }
        throw new IllegalArgumentException("Task not found: " + taskId);
    }

    public Task markTaskPending(Long taskId) {
        Optional<Task> taskOpt = taskDAO.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setCompleted(false);
            return taskDAO.save(task);
        }
        throw new IllegalArgumentException("Task not found: " + taskId);
    }

    public boolean deleteTask(Long taskId) {
        return taskDAO.deleteById(taskId);
    }

    public int deleteAllCompletedTasks() {
        return taskDAO.deleteCompletedTasks();
    }

    public TaskStatistics getStatistics() {
        long total = taskDAO.getTotalCount();
        long completed = taskDAO.getCompletedCount();
        long pending = taskDAO.getPendingCount();
        long overdue = taskDAO.findOverdueTasks().size();
        long dueToday = taskDAO.findTasksDueToday().size();

        return new TaskStatistics(total, completed, pending, overdue, dueToday);
    }

    public boolean isUsingDatabaseStorage() {
        return useDatabaseStorage;
    }

    // Inner classes for filtering and sorting
    public enum TaskSortCriteria {
        TITLE, PRIORITY, DUE_DATE, CREATED_DATE, COMPLETED_DATE
    }

    public enum TaskFilter {
        ALL, PENDING, COMPLETED, OVERDUE, DUE_TODAY, HIGH_PRIORITY, URGENT
    }

    public static class TaskStatistics {
        private final long total;
        private final long completed;
        private final long pending;
        private final long overdue;
        private final long dueToday;

        public TaskStatistics(long total, long completed, long pending, long overdue, long dueToday) {
            this.total = total;
            this.completed = completed;
            this.pending = pending;
            this.overdue = overdue;
            this.dueToday = dueToday;
        }

        // Getters
        public long getTotal() { return total; }
        public long getCompleted() { return completed; }
        public long getPending() { return pending; }
        public long getOverdue() { return overdue; }
        public long getDueToday() { return dueToday; }
        
        public double getCompletionPercentage() {
            return total > 0 ? (double) completed / total * 100 : 0;
        }
    }
}
