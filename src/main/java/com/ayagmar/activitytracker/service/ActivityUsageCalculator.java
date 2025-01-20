package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.process.MonitorActivity;
import com.ayagmar.activitytracker.util.DateTimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
    public ApplicationUsageReport calculateReport(DateTimeRange dateRange, List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            return ApplicationUsageReport.createEmptyReport(dateRange);
        }

        Map<String, Duration> appUsageDurations = new HashMap<>();
        Duration totalActiveDuration = Duration.ZERO;
        Duration totalIdleDuration = Duration.ZERO;

        for (int i = 0; i < logs.size() - 1; i++) {
            ActivityLog currentLog = logs.get(i);
            ActivityLog nextLog = logs.get(i + 1);
            Duration duration = Duration.between(currentLog.getTimestamp(), nextLog.getTimestamp());

            if (currentLog.isIdle()) {
                totalIdleDuration = totalIdleDuration.plus(duration);
            } else if (hasFocusedWindow(currentLog)) {
                totalActiveDuration = totalActiveDuration.plus(duration);
                processFocusedApplication(currentLog, duration, appUsageDurations);
            }
        }

        if (!logs.isEmpty()) {
            ActivityLog lastLog = logs.getLast();
            Duration defaultDuration = Duration.ofMinutes(1);

            if (lastLog.isIdle()) {
                totalIdleDuration = totalIdleDuration.plus(defaultDuration);
            } else if (hasFocusedWindow(lastLog)) {
                totalActiveDuration = totalActiveDuration.plus(defaultDuration);
                processFocusedApplication(lastLog, defaultDuration, appUsageDurations);
            }
        }

        List<ApplicationUsageStat> appStats = createAndAdjustApplicationStats(appUsageDurations, totalActiveDuration);

        return ApplicationUsageReport.builder()
                .startDate(dateRange.getStart())
                .endDate(dateRange.getEnd())
                .totalActiveMinutes(convertToMinutes(totalActiveDuration))
                .totalIdleMinutes(convertToMinutes(totalIdleDuration))
                .applicationStats(appStats)
                .build();
    }

    private boolean hasFocusedWindow(ActivityLog log) {
        return log.getMonitorActivities().values().stream()
                .anyMatch(MonitorActivity::isFocused);
    }

    private void processFocusedApplication(ActivityLog log, Duration duration, Map<String, Duration> appUsageDurations) {
        log.getMonitorActivities().values().stream()
                .filter(MonitorActivity::isFocused)
                .map(MonitorActivity::getApplicationName)
                .forEach(appName ->
                        appUsageDurations.merge(appName, duration, Duration::plus));
    }

    private List<ApplicationUsageStat> createAndAdjustApplicationStats(Map<String, Duration> appUsageDurations, Duration totalActiveDuration) {
        List<ApplicationUsageStat> stats = createApplicationStats(appUsageDurations, totalActiveDuration);
        adjustUsagePercentages(stats);
        return stats;
    }

    private List<ApplicationUsageStat> createApplicationStats(Map<String, Duration> appUsageDurations, Duration totalActiveDuration) {
        return appUsageDurations.entrySet().stream()
                .filter(entry -> !entry.getValue().isZero())
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ApplicationUsageStat.builder()
                                        .applicationName(entry.getKey())
                                        .usageDurationInMinutes(convertToMinutes(entry.getValue()))
                                        .usagePercentage(calculatePercentage(entry.getValue(), totalActiveDuration))
                                        .build(),
                                (a, b) -> a,
                                () -> new TreeMap<>(
                                        Comparator.<String, Duration>comparing(appUsageDurations::get).reversed()
                                )
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private int convertToMinutes(Duration duration) {
        return (int) duration.toMinutes();
    }

    private double calculatePercentage(Duration duration, Duration totalActiveDuration) {
        if (totalActiveDuration.isZero()) {
            return 0.0;
        }
        return (duration.toMillis() * 100.0) / totalActiveDuration.toMillis();
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