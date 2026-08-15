package com.taskmanager.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * يمثل جلسة عمل واحدة (Session) على مهمة معينة، تُستخدم لحساب
 * الوقت الفعلي المستغرق ولإعداد التقارير وتحليل الأنماط.
 */
public class TimeEntry {

    private int id;
    private int taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isPomodoroBreak;

    public TimeEntry() {}

    public TimeEntry(int taskId, LocalDateTime startTime) {
        this.taskId = taskId;
        this.startTime = startTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isPomodoroBreak() { return isPomodoroBreak; }
    public void setPomodoroBreak(boolean pomodoroBreak) { isPomodoroBreak = pomodoroBreak; }

    /** مدة الجلسة بالدقائق (0 إن كانت لم تنتهِ بعد) */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) return 0;
        return Duration.between(startTime, endTime).toMinutes();
    }
}
