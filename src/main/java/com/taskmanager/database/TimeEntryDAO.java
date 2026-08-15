package com.taskmanager.database;

import com.taskmanager.model.TimeEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeEntryDAO {

    private static final Logger LOGGER = Logger.getLogger(TimeEntryDAO.class.getName());

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    public TimeEntry startEntry(int taskId) {
        String sql = "INSERT INTO time_entries (task_id, start_time, is_pomodoro_break) VALUES (?,?,0)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setInt(1, taskId);
            ps.setString(2, now.toString());
            ps.executeUpdate();
            TimeEntry entry = new TimeEntry(taskId, now);
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) entry.setId(keys.getInt(1));
            }
            return entry;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل بدء جلسة وقت جديدة", e);
            throw new TaskDAO.DataAccessException("تعذّر بدء المؤقت", e);
        }
    }

    public void endEntry(int entryId, LocalDateTime endTime) {
        String sql = "UPDATE time_entries SET end_time=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, endTime.toString());
            ps.setInt(2, entryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل إنهاء جلسة الوقت رقم " + entryId, e);
            throw new TaskDAO.DataAccessException("تعذّر إيقاف المؤقت", e);
        }
    }

    public List<TimeEntry> findByTask(int taskId) {
        String sql = "SELECT * FROM time_entries WHERE task_id=? ORDER BY start_time";
        List<TimeEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) entries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل قراءة سجلات وقت المهمة " + taskId, e);
            throw new TaskDAO.DataAccessException("تعذّر تحميل سجلات الوقت", e);
        }
        return entries;
    }

    public List<TimeEntry> findAllBetween(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT * FROM time_entries WHERE start_time BETWEEN ? AND ? ORDER BY start_time";
        List<TimeEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, from.toString());
            ps.setString(2, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) entries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل قراءة سجلات الوقت للفترة المطلوبة", e);
            throw new TaskDAO.DataAccessException("تعذّر تحميل سجلات الوقت", e);
        }
        return entries;
    }

    private TimeEntry mapRow(ResultSet rs) throws SQLException {
        TimeEntry e = new TimeEntry();
        e.setId(rs.getInt("id"));
        e.setTaskId(rs.getInt("task_id"));
        e.setStartTime(LocalDateTime.parse(rs.getString("start_time")));
        String end = rs.getString("end_time");
        if (end != null) e.setEndTime(LocalDateTime.parse(end));
        e.setPomodoroBreak(rs.getInt("is_pomodoro_break") == 1);
        return e;
    }
}
