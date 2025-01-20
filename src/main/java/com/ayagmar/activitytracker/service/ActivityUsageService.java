package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import com.ayagmar.activitytracker.util.DateTimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityUsageService {
    private final ActivityLogRepository repository;
    private final Clock clock;
    private final ActivityUsageCalculator activityUsageCalculator;

    public ApplicationUsageReport calculateApplicationUsage(LocalDateTime start, LocalDateTime end) {
        DateTimeRange dateRange = createDateRange(start, end);
        List<ActivityLog> logs = fetchSortedLogs(dateRange);
        return activityUsageCalculator.calculateReport(dateRange, logs);
    }

    private DateTimeRange createDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return DateTimeRange.ofFullDay(LocalDate.now(clock));
        }
        return DateTimeRange.of(start, end);
    }

    private List<ActivityLog> fetchSortedLogs(DateTimeRange dateRange) {
        return repository.findByTimestampBetween(dateRange.getStart(), dateRange.getEnd())
                .stream()
                .sorted(Comparator.comparing(ActivityLog::getTimestamp))
                .toList();
    }
}