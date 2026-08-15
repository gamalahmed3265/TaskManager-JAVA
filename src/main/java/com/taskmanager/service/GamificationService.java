package com.taskmanager.service;

import com.taskmanager.database.DatabaseManager;
import com.taskmanager.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * نظام النقاط والمستويات: يمنح نقاطًا عند إنجاز المهام في وقتها،
 * ويحسب المستوى بناءً على إجمالي النقاط المتراكمة.
 */
public class GamificationService {

    private static final Logger LOGGER = Logger.getLogger(GamificationService.class.getName());
    private static final int POINTS_PER_LEVEL = 100;

    private Connection conn() { return DatabaseManager.getInstance().getConnection(); }

    /** يحسب النقاط المكتسبة عند إنجاز مهمة: أساسية + مكافأة الالتزام بالموعد + مكافأة دقة التقدير */
    public int calculatePointsForCompletion(Task task) {
        int base = switch (task.getPriority()) {
            case HIGH -> 30;
            case MEDIUM -> 20;
            case LOW -> 10;
        };
        int bonus = 0;
        if (task.getDeadline() != null && !LocalDateTime.now().isAfter(task.getDeadline())) {
            bonus += 15; // أُنجزت قبل الموعد النهائي أو في وقته
        }
        double ratio = task.getTimeAccuracyRatio();
        if (ratio > 0 && ratio <= 1.2) {
            bonus += 10; // التزام جيد بالوقت المقدّر
        }
        if (task.getPostponeCount() == 0) {
            bonus += 5; // لم تُؤجَّل أبدًا
        }
        return base + bonus;
    }

    public void addPoints(int points) {
        String sql = "UPDATE gamification SET total_points = total_points + ? WHERE id = 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.executeUpdate();
            recalculateLevel();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل تحديث النقاط", e);
        }
    }

    private void recalculateLevel() throws SQLException {
        String selectSql = "SELECT total_points FROM gamification WHERE id = 1";
        int totalPoints = 0;
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(selectSql)) {
            if (rs.next()) totalPoints = rs.getInt("total_points");
        }
        int level = Math.max(1, totalPoints / POINTS_PER_LEVEL + 1);
        try (PreparedStatement ps = conn().prepareStatement("UPDATE gamification SET level=? WHERE id=1")) {
            ps.setInt(1, level);
            ps.executeUpdate();
        }
    }

    public static class GamificationStatus {
        public int totalPoints;
        public int level;
        public int pointsToNextLevel;
    }

    public GamificationStatus getStatus() {
        GamificationStatus status = new GamificationStatus();
        String sql = "SELECT total_points, level FROM gamification WHERE id = 1";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                status.totalPoints = rs.getInt("total_points");
                status.level = rs.getInt("level");
                status.pointsToNextLevel = (status.level * POINTS_PER_LEVEL) - status.totalPoints;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "فشل قراءة حالة النقاط", e);
        }
        return status;
    }

    /** شارات بسيطة مبنية على إجمالي النقاط (يمكن التوسّع فيها لاحقًا) */
    public String getBadgeForPoints(int totalPoints) {
        if (totalPoints >= 1000) return "🏆 أسطورة الإنتاجية";
        if (totalPoints >= 500) return "🥇 محترف منظّم";
        if (totalPoints >= 200) return "🥈 منجز نشيط";
        if (totalPoints >= 50) return "🥉 بداية موفّقة";
        return "🌱 مبتدئ";
    }
}
