package com.taskmanager.service;

import com.taskmanager.database.TimeEntryDAO;
import com.taskmanager.model.Category;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.TimeEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * يحسب مؤشرات لوحة الإحصائيات: نسبة الإنجاز، توزيع الوقت حسب التصنيف،
 * اتجاه الإنتاجية عبر الزمن، وتحليل أكثر ساعات اليوم إنتاجية للمستخدم.
 */
public class StatisticsService {

    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();

    public double completionRate(List<Task> tasks) {
        if (tasks.isEmpty()) return 0;
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        return (completed * 100.0) / tasks.size();
    }

    /** إجمالي الدقائق الفعلية المستغرقة لكل تصنيف */
    public Map<Category, Integer> timeByCategory(List<Task> tasks) {
        Map<Category, Integer> map = new EnumMap<>(Category.class);
        for (Task t : tasks) {
            map.merge(t.getCategory(), t.getActualMinutes(), Integer::sum);
        }
        return map;
    }

    /** الوقت "الضائع": فرق بين الوقت الفعلي والمقدّر عندما يتجاوز الفعلي المقدّر */
    public int wastedMinutes(List<Task> tasks) {
        return tasks.stream()
                .filter(t -> t.getActualMinutes() > t.getEstimatedMinutes())
                .mapToInt(t -> t.getActualMinutes() - t.getEstimatedMinutes())
                .sum();
    }

    /** نسبة إنجاز المهام يوميًا خلال آخر N يوم (للرسم البياني لاتجاه الإنتاجية) */
    public Map<LocalDate, Double> dailyCompletionTrend(List<Task> tasks, int days) {
        Map<LocalDate, List<Task>> byDay = tasks.stream()
                .filter(t -> t.getCreatedAt() != null)
                .collect(Collectors.groupingBy(t -> t.getCreatedAt().toLocalDate()));

        Map<LocalDate, Double> trend = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<Task> dayTasks = byDay.getOrDefault(day, List.of());
            trend.put(day, completionRate(dayTasks));
        }
        return trend;
    }

    /**
     * تحليل الأنماط: يكتشف أكثر ساعات اليوم إنتاجية بناءً على عدد جلسات العمل
     * المسجّلة في كل ساعة خلال آخر 30 يومًا.
     */
    public Map<Integer, Long> mostProductiveHours() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();
        List<TimeEntry> entries = timeEntryDAO.findAllBetween(from, to);
        Map<Integer, Long> hourCounts = new TreeMap<>();
        for (TimeEntry e : entries) {
            int hour = e.getStartTime().getHour();
            hourCounts.merge(hour, 1L, Long::sum);
        }
        return hourCounts;
    }

    public Optional<Integer> bestProductivityHour() {
        return mostProductiveHours().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }
}
