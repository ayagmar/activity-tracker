package com.ayagmar.activitytracker.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ActivityTotals {
    private LocalDate date;
    private long totalLeftClicks;
    private long totalRightClicks;
    private long totalMiddleClicks;
    private long totalKeyPresses;
    private double totalMouseMovement;

    public static ActivityTotals createEmptyTotals(LocalDate date) {
        return ActivityTotals.builder()
                .date(date)
                .totalLeftClicks(0L)
                .totalRightClicks(0L)
                .totalMiddleClicks(0L)
                .totalKeyPresses(0L)
                .totalMouseMovement(0.0)
                .build();
    }
}