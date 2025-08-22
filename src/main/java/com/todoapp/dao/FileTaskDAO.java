package com.todoapp.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import com.todoapp.util.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FileTaskDAO implements TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(FileTaskDAO.class);
    private final ObjectMapper objectMapper;
    private final String filePath;
    private final String backupPath;
    private final AtomicLong idGenerator;
    private List<Task> tasks;

    public FileTaskDAO() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        this.filePath = config.getProperty("file.storage.path");
        this.backupPath = config.getProperty("file.storage.backup.path");
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        this.tasks = new ArrayList<>();
        this.idGenerator = new AtomicLong(1);
        
        initializeStorage();
        loadTasks();
    }

    private void initializeStorage() {
        try {
            Path dataDir = Paths.get(filePath).getParent();
            if (dataDir != null && !Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                logger.info("Created data directory: {}", dataDir);
            }
        } catch (IOException e) {
            logger.error("Error creating data directory", e);
            throw new RuntimeException("Failed to initialize storage", e);
        }
    }

    private void loadTasks() {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info("Tasks file does not exist, starting with empty list");
            return;
        }

        try {
            TypeReference<List<Task>> typeRef = new TypeReference<List<Task>>() {};
            tasks = objectMapper.readValue(file, typeRef);
            
            // Update ID generator to avoid conflicts
            long maxId = tasks.stream()
                    .mapToLong(task -> task.getId() != null ? task.getId() : 0)
                    .max()
                    .orElse(0);
            idGenerator.set(maxId + 1);
            
            logger.info("Loaded {} tasks from file", tasks.size());
            
        } catch (IOException e) {
            logger.error("Error loading tasks from file", e);
            // Try to load from backup
            loadFromBackup();
        }
    }

    private void loadFromBackup() {
        File backupFile = new File(backupPath);
        if (!backupFile.exists()) {
            logger.warn("No backup file found, starting with empty list");
            return;
        }

        try {
            TypeReference<List<Task>> typeRef = new TypeReference<List<Task>>() {};
            tasks = objectMapper.readValue(backupFile, typeRef);
            logger.info("Loaded {} tasks from backup file", tasks.size());
            
            // Save to main file
            saveTasks();
            
        } catch (IOException e) {
            logger.error("Error loading tasks from backup file", e);
            tasks = new ArrayList<>();
        }
    }

    private synchronized void saveTasks() {
        try {
            // Create backup before saving
            createBackup();
            
            // Save to main file
            objectMapper.writeValue(new File(filePath), tasks);
            logger.debug("Tasks saved to file successfully");
            
        } catch (IOException e) {
            logger.error("Error saving tasks to file", e);
            throw new RuntimeException("Failed to save tasks", e);
        }
    }

    private void createBackup() {
        File mainFile = new File(filePath);
        if (mainFile.exists()) {
            try {
                Files.copy(mainFile.toPath(), Paths.get(backupPath), 
                          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Backup created successfully");
            } catch (IOException e) {
                logger.warn("Failed to create backup", e);
            }
        }
    }

    @Override
    public synchronized Task save(Task task) {
        if (task.getId() == null) {
            // New task
            task.setId(idGenerator.getAndIncrement());
            tasks.add(task);
            logger.info("New task created with ID: {}", task.getId());
        } else {
            // Update existing task
            Optional<Task> existingTask = tasks.stream()
                    .filter(t -> t.getId().equals(task.getId()))
                    .findFirst();
            
            if (existingTask.isPresent()) {
                int index = tasks.indexOf(existingTask.get());
                tasks.set(index, task);
                logger.info("Task updated with ID: {}", task.getId());
            } else {
                throw new RuntimeException("Task not found for update: " + task.getId());
            }
        }
        
        saveTasks();
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    @Override
    public List<Task> findByCompleted(boolean completed) {
        return tasks.stream()
                .filter(task -> task.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByPriority(Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(task -> !task.isCompleted() && 
                               task.getDueDate() != null && 
                               task.getDueDate().isBefore(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTasksDueToday() {
        return tasks.stream()
                .filter(Task::isDueToday)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> searchTasks(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return tasks.stream()
                .filter(task -> 
                    (task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerSearchTerm)) ||
                    (task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerSearchTerm)))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized boolean deleteById(Long id) {
        boolean removed = tasks.removeIf(task -> task.getId().equals(id));
        if (removed) {
            saveTasks();
            logger.info("Task deleted with ID: {}", id);
        }
        return removed;
    }

    @Override
    public synchronized int deleteCompletedTasks() {
        int initialSize = tasks.size();
        tasks.removeIf(Task::isCompleted);
        int deletedCount = initialSize - tasks.size();
        
        if (deletedCount > 0) {
            saveTasks();
            logger.info("Deleted {} completed tasks", deletedCount);
        }
        
        return deletedCount;
    }

    @Override
    public long getTotalCount() {
        return tasks.size();
    }

    @Override
    public long getCompletedCount() {
        return tasks.stream()
                .mapToLong(task -> task.isCompleted() ? 1 : 0)
                .sum();
    }

    @Override
    public long getPendingCount() {
        return tasks.stream()
                .mapToLong(task -> !task.isCompleted() ? 1 : 0)
                .sum();
    }
}
