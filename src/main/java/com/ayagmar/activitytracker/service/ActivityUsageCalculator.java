package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.process.MonitorActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
class ActivityUsageCalculator {
    private final Map<String, Integer> appUsageMinutes = new HashMap<>();
    private int totalActiveMinutes = 0;
    private int totalIdleMinutes = 0;

    public ApplicationUsageReport calculateReport(DateTimeRange dateRange, List<ActivityLog> logs) {
        processLogs(logs);
        List<ApplicationUsageStat> appStats = createAndAdjustApplicationStats();

        return ApplicationUsageReport.builder()
                .startDate(dateRange.getStart())
                .endDate(dateRange.getEnd())
                .totalActiveMinutes(totalActiveMinutes)
                .totalIdleMinutes(totalIdleMinutes)
                .applicationStats(appStats)
                .build();
    }

    private void processLogs(List<ActivityLog> logs) {
        logs.forEach(this::processLog);
    }

    private void processLog(ActivityLog log) {
        if (log.isIdle()) {
            totalIdleMinutes++;
            return;
        }

        if (hasFocusedWindow(log)) {
            totalActiveMinutes++;
            processFocusedApplication(log);
        }
    }

    private boolean hasFocusedWindow(ActivityLog log) {
        return log.getMonitorActivities().values().stream()
                .anyMatch(MonitorActivity::isFocused);
    }

    private void processFocusedApplication(ActivityLog log) {
        log.getMonitorActivities().values().stream()
                .filter(MonitorActivity::isFocused)
                .map(MonitorActivity::getApplicationName)
                .forEach(appName -> appUsageMinutes.merge(appName, 1, Integer::sum));
    }

    private List<ApplicationUsageStat> createAndAdjustApplicationStats() {
        List<ApplicationUsageStat> stats = createApplicationStats();
        adjustUsagePercentages(stats);
        return stats;
    }

    private List<ApplicationUsageStat> createApplicationStats() {
        return appUsageMinutes.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Filter out unused applications
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ApplicationUsageStat.builder()
                                        .applicationName(entry.getKey())
                                        .usageDurationInMinutes(entry.getValue())
                                        .usagePercentage(calculatePercentage(entry.getValue()))
                                        .build(),
                                (a, b) -> a,
                                () -> new TreeMap<>(
                                        Comparator.<String, Integer>comparing(appUsageMinutes::get).reversed()
                                )
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }


    private double calculatePercentage(int minutes) {
        return totalActiveMinutes > 0 ? (minutes * 100.0) / totalActiveMinutes : 0.0;
    }

    private void adjustUsagePercentages(List<ApplicationUsageStat> appStats) {
        double totalPercentage = calculateTotalPercentage(appStats);
        if (totalPercentage > 0) {
            normalizePercentages(appStats, totalPercentage);
        }
    }

    private double calculateTotalPercentage(List<ApplicationUsageStat> appStats) {
        return appStats.stream()
                .mapToDouble(ApplicationUsageStat::getUsagePercentage)
                .sum();
    }

    private void normalizePercentages(List<ApplicationUsageStat> appStats, double totalPercentage) {
        double adjustmentFactor = 100.0 / totalPercentage;
        appStats.forEach(stat ->
                stat.setUsagePercentage(roundToTwoDecimals(stat.getUsagePercentage() * adjustmentFactor))
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}