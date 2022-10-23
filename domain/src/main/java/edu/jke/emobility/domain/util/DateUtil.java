package edu.jke.emobility.domain.util;

import java.time.LocalDateTime;

public class DateUtil {
    public static LocalDateTime earliest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isAfter(right)) {
            return right;
        } else {
            return left;
        }
    }

    public static LocalDateTime latest(LocalDateTime left, LocalDateTime right) {
        if (left == null || left.isBefore(right)) {
            return right;
        } else {
            return left;
        }
    }
}
