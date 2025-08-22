USE todoapp;

-- Tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    due_date TIMESTAMP NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP NULL,
    
    -- Indexes for better query performance
    INDEX idx_priority (priority),
    INDEX idx_completed (completed),
    INDEX idx_due_date (due_date),
    INDEX idx_created_date (created_date),
    INDEX idx_title (title),
    
    -- Full-text search index for title and description
    FULLTEXT INDEX idx_search (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data (optional)
INSERT INTO tasks (title, description, priority, completed, due_date) VALUES
('Complete project documentation', 'Write comprehensive documentation for the todo application', 'HIGH', FALSE, DATE_ADD(NOW(), INTERVAL 3 DAY)),
('Review code quality', 'Perform code review and refactoring where necessary', 'MEDIUM', FALSE, DATE_ADD(NOW(), INTERVAL 1 WEEK)),
('Setup CI/CD pipeline', 'Configure continuous integration and deployment', 'LOW', FALSE, DATE_ADD(NOW(), INTERVAL 2 WEEK)),
('Database optimization', 'Optimize database queries and add proper indexing', 'MEDIUM', TRUE, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('User interface improvements', 'Enhance the user interface with better styling', 'HIGH', FALSE, DATE_ADD(NOW(), INTERVAL 5 DAY));

-- View to get task statistics
CREATE OR REPLACE VIEW task_statistics AS
SELECT 
    COUNT(*) as total_tasks,
    COUNT(CASE WHEN completed = TRUE THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN completed = FALSE THEN 1 END) as pending_tasks,
    COUNT(CASE WHEN completed = FALSE AND due_date < NOW() THEN 1 END) as overdue_tasks,
    COUNT(CASE WHEN completed = FALSE AND DATE(due_date) = CURDATE() THEN 1 END) as due_today_tasks,
    ROUND(
        (COUNT(CASE WHEN completed = TRUE THEN 1 END) * 100.0) / NULLIF(COUNT(*), 0), 
        2
    ) as completion_percentage
FROM tasks;

-- Stored procedure to clean up old completed tasks (optional)
DELIMITER //
CREATE PROCEDURE CleanupOldCompletedTasks(IN days_old INT)
BEGIN
    DELETE FROM tasks 
    WHERE completed = TRUE 
    AND completed_date < DATE_SUB(NOW(), INTERVAL days_old DAY);
    
    SELECT ROW_COUNT() as deleted_tasks;
END //
DELIMITER ;

-- Function to get priority level as integer
DELIMITER //
CREATE FUNCTION GetPriorityLevel(priority_name VARCHAR(20))
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    CASE priority_name
        WHEN 'LOW' THEN RETURN 1;
        WHEN 'MEDIUM' THEN RETURN 2;
        WHEN 'HIGH' THEN RETURN 3;
        WHEN 'URGENT' THEN RETURN 4;
        ELSE RETURN 2;
    END CASE;
END //
DELIMITER ;

-- View to show tasks with calculated fields
CREATE OR REPLACE VIEW task_details AS
SELECT 
    t.*,
    GetPriorityLevel(t.priority) as priority_level,
    CASE 
        WHEN t.completed = TRUE THEN 'Completed'
        WHEN t.due_date < NOW() THEN 'Overdue'
        WHEN DATE(t.due_date) = CURDATE() THEN 'Due Today'
        WHEN t.due_date BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 3 DAY) THEN 'Due Soon'
        ELSE 'Pending'
    END as status_description,
    DATEDIFF(t.due_date, NOW()) as days_until_due,
    TIMESTAMPDIFF(DAY, t.created_date, COALESCE(t.completed_date, NOW())) as age_in_days
FROM tasks t;

-- Show the created tables and views
SHOW TABLES;
SHOW CREATE VIEW task_statistics;
SHOW CREATE VIEW task_details;
