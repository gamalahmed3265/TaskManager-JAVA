package com.taskmanager.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String formatTime(LocalDateTime dt) {
        return dt == null ? "--:--" : dt.format(TIME_FORMAT);
    }

    public static String formatDate(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FORMAT);
    }

    public static String minutesToHoursMinutes(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        if (h == 0) return m + " دقيقة";
        return h + " س " + m + " د";
    }
}
