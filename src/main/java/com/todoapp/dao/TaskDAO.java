package com.todoapp.dao;

import com.todoapp.model.Task;
import com.todoapp.model.Priority;
import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    
    Task save(Task task);
    
    Optional<Task> findById(Long id);

    List<Task> findAll();
    
    List<Task> findByCompleted(boolean completed);
 
    List<Task> findByPriority(Priority priority);

    List<Task> findOverdueTasks();

    List<Task> findTasksDueToday();

    List<Task> searchTasks(String searchTerm);

    boolean deleteById(Long id);

    int deleteCompletedTasks();

    long getTotalCount();

    long getCompletedCount();

    long getPendingCount();
}
