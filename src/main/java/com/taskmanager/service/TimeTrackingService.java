package com.taskmanager.service;

import com.taskmanager.database.TaskDAO;
import com.taskmanager.database.TimeEntryDAO;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.TimeEntry;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * يدير مؤقتات المهام (Start/Pause/Resume) ويحسب الوقت الفعلي المستغرق،
 * ويحدّث المهمة نفسها في قاعدة البيانات عند كل إيقاف.
 */
public class TimeTrackingService {

    private final TimeEntryDAO timeEntryDAO = new TimeEntryDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    /** يحفظ الجلسة الجارية حاليًا لكل مهمة (taskId -> TimeEntry مفتوحة) */
    private final Map<Integer, TimeEntry> activeEntries = new HashMap<>();

    public void start(Task task) {
        if (activeEntries.containsKey(task.getId())) return; // مشغّل بالفعل
        TimeEntry entry = timeEntryDAO.startEntry(task.getId());
        activeEntries.put(task.getId(), entry);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskDAO.update(task);
    }

    /** إيقاف مؤقت (Pause) دون اعتبار المهمة منتهية */
    public void pause(Task task) {
        TimeEntry entry = activeEntries.remove(task.getId());
        if (entry == null) return;
        LocalDateTime end = LocalDateTime.now();
        timeEntryDAO.endEntry(entry.getId(), end);
        long seconds = java.time.Duration.between(entry.getStartTime(), end).getSeconds();
        task.setActualSeconds((int) (task.getActualSeconds() + seconds));
        taskDAO.update(task);
    }

    public void resume(Task task) {
        start(task);
    }

    public boolean isRunning(int taskId) {
        return activeEntries.containsKey(taskId);
    }

    public long currentSessionMinutes(int taskId) {
        TimeEntry entry = activeEntries.get(taskId);
        if (entry == null) return 0;
        return java.time.Duration.between(entry.getStartTime(), LocalDateTime.now()).toMinutes();
    }

    public long currentSessionSeconds(int taskId) {
        TimeEntry entry = activeEntries.get(taskId);
        if (entry == null) return 0;
        return java.time.Duration.between(entry.getStartTime(), LocalDateTime.now()).getSeconds();
    }
}
