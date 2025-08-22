package com.todoapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    
    private Task task;
    
    @BeforeEach
    void setUp() {
        task = new Task("Test Task", "Test Description");
    }
    
    @Test
    void testTaskCreation() {
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals("Test Description", task.getDescription());
        assertEquals(Priority.MEDIUM, task.getPriority());
        assertFalse(task.isCompleted());
        assertNotNull(task.getCreatedDate());
        assertNull(task.getCompletedDate());
    }
    
    @Test
    void testTaskWithPriorityAndDueDate() {
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        Task taskWithDetails = new Task("Priority Task", "Description", Priority.HIGH, dueDate);
        
        assertEquals(Priority.HIGH, taskWithDetails.getPriority());
        assertEquals(dueDate, taskWithDetails.getDueDate());
    }
    
    @Test
    void testMarkTaskCompleted() {
        assertFalse(task.isCompleted());
        assertNull(task.getCompletedDate());
        
        task.setCompleted(true);
        
        assertTrue(task.isCompleted());
        assertNotNull(task.getCompletedDate());
    }
    
    @Test
    void testMarkTaskPending() {
        task.setCompleted(true);
        assertTrue(task.isCompleted());
        assertNotNull(task.getCompletedDate());
        
        task.setCompleted(false);
        
        assertFalse(task.isCompleted());
        assertNull(task.getCompletedDate());
    }
    
    @Test
    void testOverdueTask() {
        // Task with past due date
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        task.setDueDate(pastDate);
        
        assertTrue(task.isOverdue());
        
        // Completed task should not be overdue
        task.setCompleted(true);
        assertFalse(task.isOverdue());
    }
    
    @Test
    void testTaskDueToday() {
        LocalDateTime today = LocalDateTime.now();
        task.setDueDate(today);
        
        assertTrue(task.isDueToday());
        
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        task.setDueDate(tomorrow);
        
        assertFalse(task.isDueToday());
    }
    
    @Test
    void testTaskDueSoon() {
        LocalDateTime soonDate = LocalDateTime.now().plusDays(2);
        task.setDueDate(soonDate);
        
        assertTrue(task.isDueSoon());
        
        LocalDateTime farDate = LocalDateTime.now().plusDays(5);
        task.setDueDate(farDate);
        
        assertFalse(task.isDueSoon());
    }
    
    @Test
    void testTaskEquality() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        
        // Tasks without IDs should not be equal
        assertNotEquals(task1, task2);
        
        // Tasks with same ID should be equal
        task1.setId(1L);
        task2.setId(1L);
        assertEquals(task1, task2);
        
        // Tasks with different IDs should not be equal
        task2.setId(2L);
        assertNotEquals(task1, task2);
    }
    
    @Test
    void testTaskCopy() {
        task.setId(1L);
        task.setPriority(Priority.HIGH);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setCompleted(true);
        
        Task copy = task.copy();
        
        assertEquals(task.getId(), copy.getId());
        assertEquals(task.getTitle(), copy.getTitle());
        assertEquals(task.getDescription(), copy.getDescription());
        assertEquals(task.getPriority(), copy.getPriority());
        assertEquals(task.getDueDate(), copy.getDueDate());
        assertEquals(task.isCompleted(), copy.isCompleted());
        assertEquals(task.getCreatedDate(), copy.getCreatedDate());
        assertEquals(task.getCompletedDate(), copy.getCompletedDate());
        
        // Ensure it's a different object
        assertNotSame(task, copy);
    }
    
    @Test
    void testTaskToString() {
        task.setId(1L);
        task.setPriority(Priority.HIGH);
        
        String taskString = task.toString();
        
        assertTrue(taskString.contains("id=1"));
        assertTrue(taskString.contains("title='Test Task'"));
        assertTrue(taskString.contains("priority=HIGH"));
        assertTrue(taskString.contains("completed=false"));
    }
}
