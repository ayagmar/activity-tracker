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
}