package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.model.MonitorActivity;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationUsageService {
    private final ActivityLogRepository repository;

    public ApplicationUsageReport calculateApplicationUsage(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            end = LocalDateTime.now();
            start = end.minusDays(1);
        } else if (start == null) {
            start = end.minusDays(1);
        } else if (end == null) {
            end = start.plusDays(1);
        }

        List<ActivityLog> logs = repository.findByTimestampBetween(start, end);
        if (logs.isEmpty()) {
            return ApplicationUsageReport.builder()
                    .startDate(start)
                    .endDate(end)
                    .totalActiveMinutes(0)
                    .totalIdleMinutes(0)
                    .applicationStats(List.of())
                    .build();
        }

        // Calculate total minutes in the period
        long totalMinutes = ChronoUnit.MINUTES.between(start, end);

        // Track application usage with focus state
        Map<String, Long> applicationActiveMinutes = new HashMap<>();
        long totalActiveMinutes = 0;
        long totalIdleMinutes = 0;

        // Process each log entry
        for (int i = 0; i < logs.size(); i++) {
            ActivityLog currentLog = logs.get(i);
            ActivityLog nextLog = (i < logs.size() - 1) ? logs.get(i + 1) : null;

            // Calculate duration until next log or end time
            LocalDateTime currentTime = currentLog.getTimestamp();
            LocalDateTime nextTime = (nextLog != null) ? nextLog.getTimestamp() : end;
            long minutesDuration = ChronoUnit.MINUTES.between(currentTime, nextTime);

            // Skip if the duration is 0
            if (minutesDuration == 0) continue;

            // Process idle time
            if (currentLog.isIdle()) {
                totalIdleMinutes += minutesDuration;
                continue;
            }

            // Process active time
            totalActiveMinutes += minutesDuration;

            // Process application usage for each monitor
            Map<String, MonitorActivity> monitorActivities = currentLog.getActivity();
            for (MonitorActivity activity : monitorActivities.values()) {
                if (activity.focused()) {
                    String appName = activity.activeApplication();
                    applicationActiveMinutes.merge(appName, minutesDuration, Long::sum);
                }
            }
        }

        // Calculate application usage statistics
        long finalTotalActiveMinutes = totalActiveMinutes;
        List<ApplicationUsageStat> applicationStats = applicationActiveMinutes.entrySet().stream()
                .map(entry -> {
                    double usagePercentage = (finalTotalActiveMinutes > 0)
                            ? (entry.getValue() * 100.0) / finalTotalActiveMinutes
                            : 0.0;

                    return ApplicationUsageStat.builder()
                            .applicationName(entry.getKey())
                            .usageDurationInMinutes(entry.getValue())
                            .usagePercentage(Math.round(usagePercentage * 100.0) / 100.0) // Round to 2 decimal places
                            .build();
                })
                .sorted(Comparator.comparingLong(ApplicationUsageStat::getUsageDurationInMinutes).reversed())
                .collect(Collectors.toList());

        return ApplicationUsageReport.builder()
                .startDate(start)
                .endDate(end)
                .totalActiveMinutes(totalActiveMinutes)
                .totalIdleMinutes(totalIdleMinutes)
                .applicationStats(applicationStats)
                .build();
    }

}
