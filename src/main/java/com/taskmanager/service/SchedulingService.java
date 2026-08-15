package com.taskmanager.service;

import com.taskmanager.model.Task;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * تُوزّع المهام تلقائيًا على ساعات اليوم بالاعتماد على:
 *  - أوقات الذروة الإنتاجية التي يحدّدها المستخدم
 *  - مدة كل مهمة المقدّرة
 *  - فترة راحة بين المهام (اختياريًا بأسلوب Pomodoro)
 *
 * كما توفّر خوارزمية بسيطة لاقتراح أفضل وقت متاح لمهمة جديدة بحسب الفراغات.
 */
public class SchedulingService {

    public static class PeakHours {
        public final LocalTime start;
        public final LocalTime end;
        public PeakHours(LocalTime start, LocalTime end) { this.start = start; this.end = end; }
    }

    /** إعدادات Pomodoro: مدة الجلسة ومدة الراحة (بالدقائق). فعّلها بتمرير usePomodoro=true */
    public static class SchedulingOptions {
        public boolean usePomodoro = false;
        public int pomodoroSessionMinutes = 25;
        public int pomodoroBreakMinutes = 5;
        public int regularBreakMinutes = 10; // راحة بين المهام عند عدم استخدام Pomodoro
        public LocalTime dayStart = LocalTime.of(9, 0);
        public LocalTime dayEnd = LocalTime.of(22, 0);
    }

    /**
     * يرتّب المهام (بالأولوية أولًا) ويوزّعها زمنيًا بدءًا من بداية اليوم،
     * مع تفضيل وضعها داخل فترة الذروة الإنتاجية إن أمكن.
     */
    public List<Task> autoSchedule(List<Task> tasks, LocalDate date, PeakHours peak, SchedulingOptions options) {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparingInt((Task t) -> t.getPriority().getWeight()).reversed());

        LocalDateTime cursor = LocalDateTime.of(date, options.dayStart);
        LocalDateTime dayEndDt = LocalDateTime.of(date, options.dayEnd);
        LocalDateTime peakStart = peak != null ? LocalDateTime.of(date, peak.start) : null;
        LocalDateTime peakEnd = peak != null ? LocalDateTime.of(date, peak.end) : null;

        // ضع المهام عالية الأولوية أولًا داخل نافذة الذروة إن كانت متاحة
        if (peakStart != null && peakStart.isAfter(cursor)) {
            cursor = peakStart;
        }

        for (Task task : sorted) {
            if (cursor.isAfter(dayEndDt) || cursor.plusMinutes(task.getEstimatedMinutes()).isAfter(dayEndDt)) {
                continue; // لا يوجد وقت كافٍ متبقٍ في اليوم لهذه المهمة
            }
            task.setScheduledStart(cursor);
            cursor = cursor.plusMinutes(task.getEstimatedMinutes());

            if (options.usePomodoro) {
                cursor = addPomodoroBreaks(cursor, task.getEstimatedMinutes(), options);
            } else {
                cursor = cursor.plusMinutes(options.regularBreakMinutes);
            }
        }
        return sorted;
    }

    private LocalDateTime addPomodoroBreaks(LocalDateTime afterTaskEnd, int taskMinutes, SchedulingOptions options) {
        int sessions = (int) Math.ceil((double) taskMinutes / options.pomodoroSessionMinutes);
        int totalBreakMinutes = Math.max(1, sessions) * options.pomodoroBreakMinutes;
        return afterTaskEnd.plusMinutes(totalBreakMinutes);
    }

    /**
     * يبحث عن أفضل فراغ متاح لمهمة جديدة بمدة معينة، بناءً على المهام الموجودة بالفعل في اليوم.
     * يُفضَّل الفراغ الواقع داخل ساعات الذروة إن وُجد أكثر من خيار.
     */
    public LocalDateTime suggestBestSlot(List<Task> existingTasks, LocalDate date, int durationMinutes,
                                          PeakHours peak, SchedulingOptions options) {
        List<Task> scheduled = existingTasks.stream()
                .filter(t -> t.getScheduledStart() != null && t.getScheduledStart().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Task::getScheduledStart))
                .toList();

        LocalDateTime dayStart = LocalDateTime.of(date, options.dayStart);
        LocalDateTime dayEnd = LocalDateTime.of(date, options.dayEnd);

        List<LocalDateTime[]> freeSlots = new ArrayList<>();
        LocalDateTime cursor = dayStart;
        for (Task t : scheduled) {
            if (Duration.between(cursor, t.getScheduledStart()).toMinutes() >= durationMinutes) {
                freeSlots.add(new LocalDateTime[]{cursor, t.getScheduledStart()});
            }
            LocalDateTime taskEnd = t.getScheduledStart().plusMinutes(t.getEstimatedMinutes());
            if (taskEnd.isAfter(cursor)) cursor = taskEnd;
        }
        if (Duration.between(cursor, dayEnd).toMinutes() >= durationMinutes) {
            freeSlots.add(new LocalDateTime[]{cursor, dayEnd});
        }

        if (freeSlots.isEmpty()) return null;

        // تفضيل الفراغ الذي يتقاطع مع ساعات الذروة
        if (peak != null) {
            LocalDateTime peakStart = LocalDateTime.of(date, peak.start);
            LocalDateTime peakEnd = LocalDateTime.of(date, peak.end);
            for (LocalDateTime[] slot : freeSlots) {
                if (!slot[0].isAfter(peakEnd) && !slot[1].isBefore(peakStart)) {
                    LocalDateTime candidate = slot[0].isBefore(peakStart) ? peakStart : slot[0];
                    if (Duration.between(candidate, slot[1]).toMinutes() >= durationMinutes) {
                        return candidate;
                    }
                }
            }
        }
        return freeSlots.get(0)[0];
    }
}
