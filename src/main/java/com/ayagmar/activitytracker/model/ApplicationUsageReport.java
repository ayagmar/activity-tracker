package com.ayagmar.activitytracker.model;

import com.ayagmar.activitytracker.service.DateTimeRange;
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

    public static ApplicationUsageReport createEmptyReport(DateTimeRange dateRange) {
        return ApplicationUsageReport.builder()
                .startDate(dateRange.getStart())
                .endDate(dateRange.getEnd())
                .totalActiveMinutes(0)
                .totalIdleMinutes(0)
                .applicationStats(List.of())
                .build();
    }
}
