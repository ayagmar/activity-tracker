package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.process.MonitorActivity;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import com.ayagmar.activitytracker.util.DateTimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityUsageService {
    private final ActivityLogRepository repository;
    public ApplicationUsageReport calculateApplicationUsage(LocalDateTime start, LocalDateTime end) {
        DateTimeRange dateRange = DateTimeRange.createDateRange(start, end);

        List<ActivityLog> activityLogs = repository.findByTimestampBetweenOrderByTimestamp(dateRange.getStart(), dateRange.getEnd());
        if (activityLogs.isEmpty()) {
            return ApplicationUsageReport.createEmptyReport(dateRange);
        }

        Map<String, Long> appUsageDurations = new HashMap<>();
        long totalActiveMinutes = 0;
        long totalIdleMinutes = 0;

        for (int i = 0; i < activityLogs.size(); i++) {
            ActivityLog currentLog = activityLogs.get(i);

            LocalDateTime nextTimestamp = (i < activityLogs.size() - 1)
                    ? activityLogs.get(i + 1).getTimestamp()
                    : currentLog.getTimestamp();
            long durationMinutes = Math.max(1, Duration.between(currentLog.getTimestamp(), nextTimestamp).toMinutes());

            if (currentLog.isIdle()) {
                totalIdleMinutes += durationMinutes;
            } else {
                totalActiveMinutes += durationMinutes;

                currentLog.getMonitorActivities().values().stream()
                        .filter(MonitorActivity::isFocused)
                        .map(MonitorActivity::getApplicationName)
                        .forEach(app -> appUsageDurations.merge(app, durationMinutes, Long::sum));
            }
        }

        long finalTotalActiveMinutes = totalActiveMinutes;
        List<ApplicationUsageStat> applicationStats = appUsageDurations.entrySet().stream()
                .map(entry -> ApplicationUsageStat.builder()
                        .applicationName(entry.getKey())
                        .usageDurationInMinutes(entry.getValue())
                        .usagePercentage((double) entry.getValue() / finalTotalActiveMinutes * 100)
                        .build())
                .sorted(Comparator.comparingLong(ApplicationUsageStat::getUsageDurationInMinutes).reversed())
                .toList();

        return ApplicationUsageReport.builder()
                .startDate(dateRange.getStart())
                .endDate(dateRange.getEnd())
                .totalActiveMinutes(totalActiveMinutes)
                .totalIdleMinutes(totalIdleMinutes)
                .applicationStats(applicationStats)
                .build();
    }


}