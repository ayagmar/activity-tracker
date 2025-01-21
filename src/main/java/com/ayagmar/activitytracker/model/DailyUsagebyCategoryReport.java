package com.ayagmar.activitytracker.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
public class DailyUsagebyCategoryReport {
    private LocalDate day;
    private Map<String, Long> categoryUsageMinutes;
}
