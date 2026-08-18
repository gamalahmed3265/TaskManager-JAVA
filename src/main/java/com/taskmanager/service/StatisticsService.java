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
 * Ã™Å Ã˜Â­Ã˜Â³Ã˜Â¨ Ã™â€¦Ã˜Â¤Ã˜Â´Ã˜Â±Ã˜Â§Ã˜Âª Ã™â€žÃ™Ë†Ã˜Â­Ã˜Â© Ã˜Â§Ã™â€žÃ˜Â¥Ã˜Â­Ã˜ÂµÃ˜Â§Ã˜Â¦Ã™Å Ã˜Â§Ã˜Âª: Ã™â€ Ã˜Â³Ã˜Â¨Ã˜Â© Ã˜Â§Ã™â€žÃ˜Â¥Ã™â€ Ã˜Â¬Ã˜Â§Ã˜Â²Ã˜Å’ Ã˜ÂªÃ™Ë†Ã˜Â²Ã™Å Ã˜Â¹ Ã˜Â§Ã™â€žÃ™Ë†Ã™â€šÃ˜Âª Ã˜Â­Ã˜Â³Ã˜Â¨ Ã˜Â§Ã™â€žÃ˜ÂªÃ˜ÂµÃ™â€ Ã™Å Ã™ÂÃ˜Å’
 * Ã˜Â§Ã˜ÂªÃ˜Â¬Ã˜Â§Ã™â€¡ Ã˜Â§Ã™â€žÃ˜Â¥Ã™â€ Ã˜ÂªÃ˜Â§Ã˜Â¬Ã™Å Ã˜Â© Ã˜Â¹Ã˜Â¨Ã˜Â± Ã˜Â§Ã™â€žÃ˜Â²Ã™â€¦Ã™â€ Ã˜Å’ Ã™Ë†Ã˜ÂªÃ˜Â­Ã™â€žÃ™Å Ã™â€ž Ã˜Â£Ã™Æ’Ã˜Â«Ã˜Â± Ã˜Â³Ã˜Â§Ã˜Â¹Ã˜Â§Ã˜Âª Ã˜Â§Ã™â€žÃ™Å Ã™Ë†Ã™â€¦ Ã˜Â¥Ã™â€ Ã˜ÂªÃ˜Â§Ã˜Â¬Ã™Å Ã˜Â© Ã™â€žÃ™â€žÃ™â€¦Ã˜Â³Ã˜ÂªÃ˜Â®Ã˜Â¯Ã™â€¦.
 */
public class StatisticsService {

    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();

    public double completionRate(List<Task> tasks) {
        if (tasks.isEmpty()) return 0;
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        return (completed * 100.0) / tasks.size();
    }

    /** Ã˜Â¥Ã˜Â¬Ã™â€¦Ã˜Â§Ã™â€žÃ™Å  Ã˜Â§Ã™â€žÃ˜Â¯Ã™â€šÃ˜Â§Ã˜Â¦Ã™â€š Ã˜Â§Ã™â€žÃ™ÂÃ˜Â¹Ã™â€žÃ™Å Ã˜Â© Ã˜Â§Ã™â€žÃ™â€¦Ã˜Â³Ã˜ÂªÃ˜ÂºÃ˜Â±Ã™â€šÃ˜Â© Ã™â€žÃ™Æ’Ã™â€ž Ã˜ÂªÃ˜ÂµÃ™â€ Ã™Å Ã™Â */
    public Map<Category, Integer> timeByCategory(List<Task> tasks) {
        Map<Category, Integer> map = new EnumMap<>(Category.class);
        for (Task t : tasks) {
            map.merge(t.getCategory(), t.getActualSeconds(), Integer::sum);
        }
        return map;
    }

    /** Ã˜Â§Ã™â€žÃ™Ë†Ã™â€šÃ˜Âª "Ã˜Â§Ã™â€žÃ˜Â¶Ã˜Â§Ã˜Â¦Ã˜Â¹": Ã™ÂÃ˜Â±Ã™â€š Ã˜Â¨Ã™Å Ã™â€  Ã˜Â§Ã™â€žÃ™Ë†Ã™â€šÃ˜Âª Ã˜Â§Ã™â€žÃ™ÂÃ˜Â¹Ã™â€žÃ™Å  Ã™Ë†Ã˜Â§Ã™â€žÃ™â€¦Ã™â€šÃ˜Â¯Ã™â€˜Ã˜Â± Ã˜Â¹Ã™â€ Ã˜Â¯Ã™â€¦Ã˜Â§ Ã™Å Ã˜ÂªÃ˜Â¬Ã˜Â§Ã™Ë†Ã˜Â² Ã˜Â§Ã™â€žÃ™ÂÃ˜Â¹Ã™â€žÃ™Å  Ã˜Â§Ã™â€žÃ™â€¦Ã™â€šÃ˜Â¯Ã™â€˜Ã˜Â± */
    public int wastedMinutes(List<Task> tasks) {
        return tasks.stream()
                .filter(t -> t.getActualSeconds() > t.getEstimatedMinutes())
                .mapToInt(t -> t.getActualSeconds() - t.getEstimatedMinutes())
                .sum();
    }

    /** Ã™â€ Ã˜Â³Ã˜Â¨Ã˜Â© Ã˜Â¥Ã™â€ Ã˜Â¬Ã˜Â§Ã˜Â² Ã˜Â§Ã™â€žÃ™â€¦Ã™â€¡Ã˜Â§Ã™â€¦ Ã™Å Ã™Ë†Ã™â€¦Ã™Å Ã™â€¹Ã˜Â§ Ã˜Â®Ã™â€žÃ˜Â§Ã™â€ž Ã˜Â¢Ã˜Â®Ã˜Â± N Ã™Å Ã™Ë†Ã™â€¦ (Ã™â€žÃ™â€žÃ˜Â±Ã˜Â³Ã™â€¦ Ã˜Â§Ã™â€žÃ˜Â¨Ã™Å Ã˜Â§Ã™â€ Ã™Å  Ã™â€žÃ˜Â§Ã˜ÂªÃ˜Â¬Ã˜Â§Ã™â€¡ Ã˜Â§Ã™â€žÃ˜Â¥Ã™â€ Ã˜ÂªÃ˜Â§Ã˜Â¬Ã™Å Ã˜Â©) */
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
     * Ã˜ÂªÃ˜Â­Ã™â€žÃ™Å Ã™â€ž Ã˜Â§Ã™â€žÃ˜Â£Ã™â€ Ã™â€¦Ã˜Â§Ã˜Â·: Ã™Å Ã™Æ’Ã˜ÂªÃ˜Â´Ã™Â Ã˜Â£Ã™Æ’Ã˜Â«Ã˜Â± Ã˜Â³Ã˜Â§Ã˜Â¹Ã˜Â§Ã˜Âª Ã˜Â§Ã™â€žÃ™Å Ã™Ë†Ã™â€¦ Ã˜Â¥Ã™â€ Ã˜ÂªÃ˜Â§Ã˜Â¬Ã™Å Ã˜Â© Ã˜Â¨Ã™â€ Ã˜Â§Ã˜Â¡Ã™â€¹ Ã˜Â¹Ã™â€žÃ™â€° Ã˜Â¹Ã˜Â¯Ã˜Â¯ Ã˜Â¬Ã™â€žÃ˜Â³Ã˜Â§Ã˜Âª Ã˜Â§Ã™â€žÃ˜Â¹Ã™â€¦Ã™â€ž
     * Ã˜Â§Ã™â€žÃ™â€¦Ã˜Â³Ã˜Â¬Ã™â€˜Ã™â€žÃ˜Â© Ã™ÂÃ™Å  Ã™Æ’Ã™â€ž Ã˜Â³Ã˜Â§Ã˜Â¹Ã˜Â© Ã˜Â®Ã™â€žÃ˜Â§Ã™â€ž Ã˜Â¢Ã˜Â®Ã˜Â± 30 Ã™Å Ã™Ë†Ã™â€¦Ã™â€¹Ã˜Â§.
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

    /**
     * جدول زمني لليوم الحالي يوضح فترات العمل على المهام والوقت المتبقي.
     */
    public List<String> getDailyTimelineStrings(List<Task> allTasks) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<TimeEntry> entries = timeEntryDAO.findAllBetween(startOfDay, endOfDay);
        
        Map<Integer, String> taskNames = allTasks.stream().collect(Collectors.toMap(Task::getId, Task::getTitle));
        List<String> timeline = new ArrayList<>();
        long totalMinutes = 0;
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        
        for (TimeEntry entry : entries) {
            if (entry.getEndTime() != null) {
                String name = taskNames.getOrDefault(entry.getTaskId(), "مهمة غير معروفة");
                String line = String.format("%s - %s : %s (%d دقيقة)", 
                    entry.getStartTime().format(formatter), 
                    entry.getEndTime().format(formatter), 
                    name, 
                    entry.getDurationMinutes());
                timeline.add(line);
                totalMinutes += entry.getDurationMinutes();
            } else {
                // For currently running tasks
                long runningMinutes = java.time.Duration.between(entry.getStartTime(), LocalDateTime.now()).toMinutes();
                String name = taskNames.getOrDefault(entry.getTaskId(), "مهمة غير معروفة");
                String line = String.format("%s - الآن : %s (%d دقيقة حتى الآن) [قيد التنفيذ]", 
                    entry.getStartTime().format(formatter), 
                    name, 
                    runningMinutes);
                timeline.add(line);
                totalMinutes += runningMinutes;
            }
        }
        
        long freeTimeMinutes = (24 * 60) - totalMinutes;
        List<String> result = new ArrayList<>();
        result.add("إجمالي وقت العمل اليوم: " + (totalMinutes / 60) + " ساعة و " + (totalMinutes % 60) + " دقيقة");
        result.add("الوقت المتبقي من اليوم: " + (freeTimeMinutes / 60) + " ساعة و " + (freeTimeMinutes % 60) + " دقيقة");
        result.add("-------------------------------------------------");
        if (timeline.isEmpty()) {
            result.add("لا توجد جلسات عمل مسجلة لليوم.");
        } else {
            result.addAll(timeline);
        }
        
        return result;
    }
}
