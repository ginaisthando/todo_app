package com.todoapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private boolean completed;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime dueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedDate;

    // Constructors
    public Task() {
        this.createdDate = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
        this.completed = false;
    }

    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public Task(String title, String description, Priority priority, LocalDateTime dueDate) {
        this(title, description);
        this.priority = priority;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedDate == null) {
            this.completedDate = LocalDateTime.now();
        } else if (!completed) {
            this.completedDate = null;
        }
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    // Business logic methods
    public boolean isOverdue() {
        return dueDate != null && !completed && LocalDateTime.now().isAfter(dueDate);
    }

    public boolean isDueToday() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.toLocalDate().equals(now.toLocalDate());
    }

    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(3));
    }

    // Override methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", completed=" + completed +
                ", dueDate=" + dueDate +
                '}';
    }

    // Copy method for editing
    public Task copy() {
        Task copy = new Task();
        copy.setId(this.id);
        copy.setTitle(this.title);
        copy.setDescription(this.description);
        copy.setPriority(this.priority);
        copy.setCompleted(this.completed);
        copy.setDueDate(this.dueDate);
        copy.setCreatedDate(this.createdDate);
        copy.setCompletedDate(this.completedDate);
        return copy;
    }
}
