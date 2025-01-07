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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationUsageService {
    private final ActivityLogRepository repository;

    public ApplicationUsageReport calculateApplicationUsage(LocalDateTime start, LocalDateTime end) {
        end = Optional.ofNullable(end).orElse(LocalDateTime.now());
        start = Optional.ofNullable(start).orElse(end.minusDays(1));

        List<ActivityLog> logs = repository.findByTimestampBetween(start, end)
                .stream()
                .sorted(Comparator.comparing(ActivityLog::getTimestamp))
                .toList();

        if (logs.isEmpty()) {
            return createEmptyReport(start, end);
        }

        Map<String, Integer> appUsageMinutes = new HashMap<>();
        int totalActiveMinutes = 0;
        int totalIdleMinutes = 0;

        // Process each log as a discrete minute
        for (ActivityLog log : logs) {
            if (log.isIdle()) {
                totalIdleMinutes++;
                continue;
            }

            if (hasFocusedWindow(log)) {
                totalActiveMinutes++;
                processFocusedApplication(log, appUsageMinutes);
            }
        }

        // Create application stats and adjust the usage percentage
        List<ApplicationUsageStat> appStats = createApplicationStats(appUsageMinutes, totalActiveMinutes);

        // Adjust the total active minutes' usage percentages to sum to 100%
        adjustUsagePercentages(appStats);

        return ApplicationUsageReport.builder()
                .startDate(start)
                .endDate(end)
                .totalActiveMinutes(totalActiveMinutes)
                .totalIdleMinutes(totalIdleMinutes)
                .applicationStats(appStats)
                .build();
    }

    private boolean hasFocusedWindow(ActivityLog log) {
        return log.getActivity().values().stream().anyMatch(MonitorActivity::focused);
    }

    private void processFocusedApplication(ActivityLog log, Map<String, Integer> appUsageMinutes) {
        log.getActivity().values().stream()
                .filter(MonitorActivity::focused)
                .forEach(activity ->
                        appUsageMinutes.merge(activity.activeApplication(), 1, Integer::sum));
    }

    private List<ApplicationUsageStat> createApplicationStats(Map<String, Integer> appUsageMinutes, int totalActiveMinutes) {
        return appUsageMinutes.entrySet().stream()
                .map(entry -> createApplicationStat(entry, totalActiveMinutes))
                .sorted(Comparator.comparingLong(ApplicationUsageStat::getUsageDurationInMinutes).reversed())
                .collect(Collectors.toList());
    }

    private ApplicationUsageStat createApplicationStat(Map.Entry<String, Integer> entry, int totalActiveMinutes) {
        double percentage = totalActiveMinutes > 0
                ? (entry.getValue() * 100.0) / totalActiveMinutes
                : 0.0;

        return ApplicationUsageStat.builder()
                .applicationName(entry.getKey())
                .usageDurationInMinutes(entry.getValue())
                .usagePercentage(percentage)
                .build();
    }

    private void adjustUsagePercentages(List<ApplicationUsageStat> appStats) {
        // Calculate the total percentage across all apps
        double totalPercentage = appStats.stream()
                .mapToDouble(ApplicationUsageStat::getUsagePercentage)
                .sum();

        // Normalize percentages so they add up to 100%
        if (totalPercentage > 0) {
            double adjustmentFactor = 100.0 / totalPercentage;
            for (ApplicationUsageStat stat : appStats) {
                double adjustedPercentage = stat.getUsagePercentage() * adjustmentFactor;
                stat.setUsagePercentage(Math.round(adjustedPercentage * 100.0) / 100.0); // Round to 2 decimal places
            }
        }
    }

    private ApplicationUsageReport createEmptyReport(LocalDateTime start, LocalDateTime end) {
        return ApplicationUsageReport.builder()
                .startDate(start)
                .endDate(end)
                .totalActiveMinutes(0)
                .totalIdleMinutes(0)
                .applicationStats(List.of())
                .build();
    }
}
