package com.ayagmar.activitytracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationUsageReport {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long totalActiveMinutes;
    private long totalIdleMinutes;
    private List<ApplicationUsageStat> applicationStats;
}
