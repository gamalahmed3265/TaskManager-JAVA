package com.taskmanager.database;

import com.taskmanager.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * طبقة الوصول للبيانات الخاصة بجدول tasks.
 * كل الاستثناءات الخاصة بـ SQL تُعالج هنا وتُسجَّل، وتُرمى كـ DataAccessException
 * موحّدة لتسهيل التعامل معها في الطبقات الأعلى (Service/Controller).
 */
public class TaskDAO {

    private static final Logger LOGGER = Logger.getLogger(TaskDAO.class.getName());

    public static class DataAccessException extends RuntimeException {
        public DataAccessException(String message, Throwable cause) { super(message, cause); }
    }

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public Task insert(Task task) {
        String sql = """
            INSERT INTO tasks (title, description, priority, category, deadline, scheduled_start,
                estimated_minutes, actual_minutes, status, postpone_count, points, created_at, completed_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindTask(ps, task);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            return task;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل إدراج مهمة جديدة", e);
            throw new DataAccessException("تعذّر حفظ المهمة في قاعدة البيانات", e);
        }
    }

    public void update(Task task) {
        String sql = """
            UPDATE tasks SET title=?, description=?, priority=?, category=?, deadline=?, scheduled_start=?,
                estimated_minutes=?, actual_minutes=?, status=?, postpone_count=?, points=?, created_at=?, completed_at=?
            WHERE id=?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            bindTask(ps, task);
            ps.setInt(14, task.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل تحديث المهمة رقم " + task.getId(), e);
            throw new DataAccessException("تعذّر تحديث المهمة", e);
        }
    }

    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل حذف المهمة رقم " + taskId, e);
            throw new DataAccessException("تعذّر حذف المهمة", e);
        }
    }

    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY " +
                "CASE priority WHEN 'HIGH' THEN 3 WHEN 'MEDIUM' THEN 2 ELSE 1 END DESC, deadline ASC";
        List<Task> tasks = new ArrayList<>();
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل قراءة قائمة المهام", e);
            throw new DataAccessException("تعذّر تحميل المهام", e);
        }
        return tasks;
    }

    public List<Task> findByDate(java.time.LocalDate date) {
        String sql = "SELECT * FROM tasks WHERE date(scheduled_start) = ? OR date(deadline) = ? ORDER BY scheduled_start ASC";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل قراءة مهام اليوم " + date, e);
            throw new DataAccessException("تعذّر تحميل مهام اليوم", e);
        }
        return tasks;
    }

    private void bindTask(PreparedStatement ps, Task task) throws SQLException {
        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        ps.setString(3, task.getPriority().name());
        ps.setString(4, task.getCategory().name());
        ps.setString(5, task.getDeadline() != null ? task.getDeadline().toString() : null);
        ps.setString(6, task.getScheduledStart() != null ? task.getScheduledStart().toString() : null);
        ps.setInt(7, task.getEstimatedMinutes());
        ps.setInt(8, task.getActualMinutes());
        ps.setString(9, task.getStatus().name());
        ps.setInt(10, task.getPostponeCount());
        ps.setInt(11, task.getPoints());
        ps.setString(12, task.getCreatedAt() != null ? task.getCreatedAt().toString() : LocalDateTime.now().toString());
        ps.setString(13, task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(Priority.valueOf(rs.getString("priority")));
        t.setCategory(Category.valueOf(rs.getString("category")));
        t.setDeadline(parse(rs.getString("deadline")));
        t.setScheduledStart(parse(rs.getString("scheduled_start")));
        t.setEstimatedMinutes(rs.getInt("estimated_minutes"));
        t.setActualMinutes(rs.getInt("actual_minutes"));
        t.setStatus(TaskStatus.valueOf(rs.getString("status")));
        t.setPostponeCount(rs.getInt("postpone_count"));
        t.setPoints(rs.getInt("points"));
        t.setCreatedAt(parse(rs.getString("created_at")));
        t.setCompletedAt(parse(rs.getString("completed_at")));
        return t;
    }

    private LocalDateTime parse(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
