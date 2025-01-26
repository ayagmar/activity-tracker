package com.ayagmar.activitytracker.util;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Value
@AllArgsConstructor(staticName = "of")
public class DateTimeRange {
    LocalDateTime start;
    LocalDateTime end;


    public static DateTimeRange ofFullDay(LocalDate date) {
        return DateTimeRange.of(
                date.atStartOfDay(),
                date.atTime(LocalTime.MAX)
        );
    }

    public static DateTimeRange ofPlusMonth(LocalDate date, int months) {
        return DateTimeRange.of(
                date.atStartOfDay(),
                LocalDateTime.of(date.plusMonths(months), LocalTime.MAX));
    }

    public static DateTimeRange createDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return DateTimeRange.ofFullDay(LocalDate.now());
        }
        return DateTimeRange.of(start, end);
    }
}