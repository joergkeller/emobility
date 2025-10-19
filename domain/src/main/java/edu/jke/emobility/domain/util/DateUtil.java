package edu.jke.emobility.domain.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    /**
     * Return the earlier of the two dates, null means unbounded and is treated as the latest possible date.
     */
    public static LocalDateTime earliest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isAfter(right)) {
            return right;
        } else {
            return left;
        }
    }

    /**
     * Return the later of the two dates, null means unbounded and is treated as the earliest possible date.
     */
    public static LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isBefore(right)) {
            return right;
        } else {
            return left;
        }
    }

    /**
     * Check if instant is in the range [from, to), null means unbounded.
     */
    public static boolean isInRange(LocalTime instant, LocalTime from, LocalTime to) {
        if (from == null && to == null) return false;
        else if (from == null) return instant.isBefore(to);
        else if (to == null) return from.isBefore(instant) || from.equals(instant);
        else return (from.isBefore(instant) || from.equals(instant)) && instant.isBefore(to);
    }

    /**
     * Format a timestamp in ISO-8601 format without nanoseconds.
     * Empty string for null timestamps.
     */
    public static String format(LocalDateTime timestamp) {
        return timestamp == null ? "" : timestamp.withNano(0).format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
