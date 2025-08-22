package com.todoapp.service;

import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {
    
    private TaskService taskService;
    
    @BeforeEach
    void setUp() {
        // This will use file storage in test environment
        taskService = new TaskService();
    }
    
    @Test
    void testCreateTask() {
        String title = "Test Task";
        String description = "Test Description";
        Priority priority = Priority.HIGH;
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        
        Task task = taskService.createTask(title, description, priority, dueDate);
        
        assertNotNull(task);
        assertNotNull(task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(priority, task.getPriority());
        assertEquals(dueDate, task.getDueDate());
        assertFalse(task.isCompleted());
    }
    
    @Test
    void testCreateTaskWithEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("", "Description", Priority.MEDIUM, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null, "Description", Priority.MEDIUM, null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask("   ", "Description", Priority.MEDIUM, null);
        });
    }
    
    @Test
    void testCreateTaskWithDefaults() {
        Task task = taskService.createTask("Test Task", null, null, null);
        
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertNull(task.getDescription());
        assertEquals(Priority.MEDIUM, task.getPriority());
        assertNull(task.getDueDate());
    }
    
    @Test
    void testUpdateTask() {
        // Create a task first
        Task originalTask = taskService.createTask("Original Title", "Original Description", Priority.LOW, null);
        
        // Update the task
        originalTask.setTitle("Updated Title");
        originalTask.setDescription("Updated Description");
        originalTask.setPriority(Priority.HIGH);
        originalTask.setDueDate(LocalDateTime.now().plusDays(1));
        
        Task updatedTask = taskService.updateTask(originalTask);
        
        assertNotNull(updatedTask);
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(Priority.HIGH, updatedTask.getPriority());
        assertNotNull(updatedTask.getDueDate());
    }
    
    @Test
    void testUpdateTaskWithInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(null);
        });
        
        Task taskWithoutId = new Task("Test", "Test");
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskWithoutId);
        });
        
        Task taskWithEmptyTitle = new Task("", "Test");
        taskWithEmptyTitle.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskWithEmptyTitle);
        });
    }
    
    @Test
    void testMarkTaskCompleted() {
        Task task = taskService.createTask("Test Task", "Description", Priority.MEDIUM, null);
        Long taskId = task.getId();
        
        Task completedTask = taskService.markTaskCompleted(taskId);
        
        assertNotNull(completedTask);
        assertTrue(completedTask.isCompleted());
        assertNotNull(completedTask.getCompletedDate());
    }
    
    @Test
    void testMarkTaskPending() {
        Task task = taskService.createTask("Test Task", "Description", Priority.MEDIUM, null);
        Long taskId = task.getId();
        
        // First mark as completed
        taskService.markTaskCompleted(taskId);
        
        // Then mark as pending
        Task pendingTask = taskService.markTaskPending(taskId);
        
        assertNotNull(pendingTask);
        assertFalse(pendingTask.isCompleted());
        assertNull(pendingTask.getCompletedDate());
    }
    
    @Test
    void testMarkNonExistentTaskCompleted() {
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.markTaskCompleted(999L);
        });
    }
    
    @Test
    void testDeleteTask() {
        Task task = taskService.createTask("Test Task", "Description", Priority.MEDIUM, null);
        Long taskId = task.getId();
        
        boolean deleted = taskService.deleteTask(taskId);
        
        assertTrue(deleted);
        assertTrue(taskService.getTaskById(taskId).isEmpty());
    }
    
    @Test
    void testGetStatistics() {
        // Create some test tasks
        taskService.createTask("Task 1", "Description 1", Priority.HIGH, LocalDateTime.now().minusDays(1));
        Task task2 = taskService.createTask("Task 2", "Description 2", Priority.MEDIUM, LocalDateTime.now().plusDays(1));
        taskService.createTask("Task 3", "Description 3", Priority.LOW, null);
        
        // Mark one task as completed
        taskService.markTaskCompleted(task2.getId());
        
        TaskService.TaskStatistics stats = taskService.getStatistics();
        
        assertTrue(stats.getTotal() >= 3);
        assertTrue(stats.getCompleted() >= 1);
        assertTrue(stats.getPending() >= 2);
        assertTrue(stats.getCompletionPercentage() > 0);
    }
    
    @Test
    void testSearchTasks() {
        taskService.createTask("Java Programming", "Learn Java concepts", Priority.HIGH, null);
        taskService.createTask("Database Design", "Design database schema", Priority.MEDIUM, null);
        taskService.createTask("Web Development", "Build web application", Priority.LOW, null);
        
        // Search by title
        var javaResults = taskService.searchTasks("Java");
        assertTrue(javaResults.stream().anyMatch(task -> task.getTitle().contains("Java")));
        
        // Search by description
        var databaseResults = taskService.searchTasks("database");
        assertTrue(databaseResults.stream().anyMatch(task -> 
            task.getDescription().toLowerCase().contains("database")));
        
        // Empty search should return all tasks
        var allResults = taskService.searchTasks("");
        assertTrue(allResults.size() >= 3);
    }
    
    @Test
    void testGetFilteredTasks() {
        // Create test tasks
        Task pendingTask = taskService.createTask("Pending Task", "Description", Priority.HIGH, LocalDateTime.now().plusDays(1));
        Task completedTask = taskService.createTask("Completed Task", "Description", Priority.MEDIUM, null);
        Task overdueTask = taskService.createTask("Overdue Task", "Description", Priority.LOW, LocalDateTime.now().minusDays(1));
        
        taskService.markTaskCompleted(completedTask.getId());
        
        // Test different filters
        var allTasks = taskService.getFilteredTasks(TaskService.TaskFilter.ALL);
        assertTrue(allTasks.size() >= 3);
        
        var pendingTasks = taskService.getFilteredTasks(TaskService.TaskFilter.PENDING);
        assertTrue(pendingTasks.stream().noneMatch(Task::isCompleted));
        
        var completedTasks = taskService.getFilteredTasks(TaskService.TaskFilter.COMPLETED);
        assertTrue(completedTasks.stream().allMatch(Task::isCompleted));
        
        var overdueTasks = taskService.getFilteredTasks(TaskService.TaskFilter.OVERDUE);
        assertTrue(overdueTasks.stream().allMatch(Task::isOverdue));
        
        var highPriorityTasks = taskService.getFilteredTasks(TaskService.TaskFilter.HIGH_PRIORITY);
        assertTrue(highPriorityTasks.stream().allMatch(task -> task.getPriority() == Priority.HIGH));
    }
    
    @Test
    void testGetTasksSortedBy() {
        // Create tasks with different properties
        taskService.createTask("Z Task", "Description", Priority.LOW, LocalDateTime.now().plusDays(3));
        taskService.createTask("A Task", "Description", Priority.HIGH, LocalDateTime.now().plusDays(1));
        taskService.createTask("M Task", "Description", Priority.MEDIUM, LocalDateTime.now().plusDays(2));
        
        // Test sorting by title
        var sortedByTitle = taskService.getTasksSortedBy(TaskService.TaskSortCriteria.TITLE, true);
        assertTrue(sortedByTitle.size() >= 3);
        // First task should start with 'A' when sorted ascending
        assertTrue(sortedByTitle.get(0).getTitle().startsWith("A"));
        
        // Test sorting by priority
        var sortedByPriority = taskService.getTasksSortedBy(TaskService.TaskSortCriteria.PRIORITY, true);
        assertTrue(sortedByPriority.size() >= 3);
        // First task should have lowest priority level when sorted ascending
        assertEquals(Priority.LOW, sortedByPriority.get(0).getPriority());
        
        // Test descending sort
        var sortedByPriorityDesc = taskService.getTasksSortedBy(TaskService.TaskSortCriteria.PRIORITY, false);
        // First task should have highest priority level when sorted descending
        assertEquals(Priority.HIGH, sortedByPriorityDesc.get(0).getPriority());
    }
}
