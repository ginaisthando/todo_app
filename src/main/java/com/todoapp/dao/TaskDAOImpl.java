package com.todoapp.dao;

import com.todoapp.database.DatabaseConnection;
import com.todoapp.model.Priority;
import com.todoapp.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAOImpl implements TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(TaskDAOImpl.class);
    private final DatabaseConnection dbConnection;

    public TaskDAOImpl() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            return insert(task);
        } else {
            return update(task);
        }
    }

    private Task insert(Task task) {
        String sql = "INSERT INTO tasks (title, description, priority, completed, due_date, created_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setBoolean(4, task.isCompleted());
            stmt.setTimestamp(5, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
            stmt.setTimestamp(6, Timestamp.valueOf(task.getCreatedDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                    logger.info("Task created with ID: {}", task.getId());
                    return task;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            logger.error("Error inserting task", e);
            throw new RuntimeException("Failed to insert task", e);
        }
    }

    private Task update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, priority = ?, completed = ?, " +
                     "due_date = ?, completed_date = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setBoolean(4, task.isCompleted());
            stmt.setTimestamp(5, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
            stmt.setTimestamp(6, task.getCompletedDate() != null ? Timestamp.valueOf(task.getCompletedDate()) : null);
            stmt.setLong(7, task.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating task failed, no rows affected.");
            }

            logger.info("Task updated with ID: {}", task.getId());
            return task;

        } catch (SQLException e) {
            logger.error("Error updating task", e);
            throw new RuntimeException("Failed to update task", e);
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding task by ID: {}", id, e);
            throw new RuntimeException("Failed to find task", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY created_date DESC";
        return executeQuery(sql);
    }

    @Override
    public List<Task> findByCompleted(boolean completed) {
        String sql = "SELECT * FROM tasks WHERE completed = ? ORDER BY created_date DESC";
        return executeQuery(sql, completed);
    }

    @Override
    public List<Task> findByPriority(Priority priority) {
        String sql = "SELECT * FROM tasks WHERE priority = ? ORDER BY created_date DESC";
        return executeQuery(sql, priority.name());
    }

    @Override
    public List<Task> findOverdueTasks() {
        String sql = "SELECT * FROM tasks WHERE due_date < NOW() AND completed = FALSE ORDER BY due_date ASC";
        return executeQuery(sql);
    }

    @Override
    public List<Task> findTasksDueToday() {
        String sql = "SELECT * FROM tasks WHERE DATE(due_date) = CURDATE() AND completed = FALSE ORDER BY due_date ASC";
        return executeQuery(sql);
    }

    @Override
    public List<Task> searchTasks(String searchTerm) {
        String sql = "SELECT * FROM tasks WHERE (title LIKE ? OR description LIKE ?) ORDER BY created_date DESC";
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, searchPattern, searchPattern);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Task deleted with ID: {}", id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting task with ID: {}", id, e);
            throw new RuntimeException("Failed to delete task", e);
        }

        return false;
    }

    @Override
    public int deleteCompletedTasks() {
        String sql = "DELETE FROM tasks WHERE completed = TRUE";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int deletedCount = stmt.executeUpdate();
            logger.info("Deleted {} completed tasks", deletedCount);
            return deletedCount;

        } catch (SQLException e) {
            logger.error("Error deleting completed tasks", e);
            throw new RuntimeException("Failed to delete completed tasks", e);
        }
    }

    @Override
    public long getTotalCount() {
        return getCount("SELECT COUNT(*) FROM tasks");
    }

    @Override
    public long getCompletedCount() {
        return getCount("SELECT COUNT(*) FROM tasks WHERE completed = TRUE");
    }

    @Override
    public long getPendingCount() {
        return getCount("SELECT COUNT(*) FROM tasks WHERE completed = FALSE");
    }

    private long getCount(String sql) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            logger.error("Error executing count query: {}", sql, e);
            throw new RuntimeException("Failed to get count", e);
        }

        return 0;
    }

    private List<Task> executeQuery(String sql, Object... parameters) {
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }

        return tasks;
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        task.setCompleted(rs.getBoolean("completed"));

        Timestamp dueDate = rs.getTimestamp("due_date");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDateTime());
        }

        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            task.setCreatedDate(createdDate.toLocalDateTime());
        }

        Timestamp completedDate = rs.getTimestamp("completed_date");
        if (completedDate != null) {
            task.setCompletedDate(completedDate.toLocalDateTime());
        }

        return task;
    }
}
