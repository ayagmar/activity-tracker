package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
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
            return new ApplicationUsageReport(start, end, 0, 0, List.of());
        }

        Map<String, Long> appUsageMap = new HashMap<>();
        long totalActiveMinutes = 0;
        long totalIdleMinutes = 0;

        for (int i = 1; i < logs.size(); i++) {
            ActivityLog previousLog = logs.get(i - 1);
            ActivityLog currentLog = logs.get(i);
            long duration = ChronoUnit.MINUTES.between(previousLog.getTimestamp(), currentLog.getTimestamp());

            if (previousLog.isIdle() || currentLog.isIdle()) {
                totalIdleMinutes += duration;
            } else {
                totalActiveMinutes += duration;
                Set<String> uniqueApplications = new HashSet<>();

                for (Map<String, String> monitorData : currentLog.getMonitorActivity().values()) {
                    uniqueApplications.add(monitorData.get("activeApplication"));
                }

                for (String app : uniqueApplications) {
                    appUsageMap.merge(app, duration, Long::sum);
                }
            }
        }

        // Log to check active and idle minutes
        System.out.println("Total Active Minutes: " + totalActiveMinutes);
        System.out.println("Total Idle Minutes: " + totalIdleMinutes);

        // Ensure the usage percentage calculation is safe
        long finalTotalActiveMinutes = totalActiveMinutes;
        List<ApplicationUsageStat> stats = appUsageMap.entrySet().stream()
                .map(entry -> {
                    double usagePercentage = finalTotalActiveMinutes == 0 ? 0 : (entry.getValue() * 100.0) / finalTotalActiveMinutes;
                    // Log to check each application's stats
                    System.out.println(entry.getKey() + ": " + entry.getValue() + " minutes (" + usagePercentage + "%)");
                    return ApplicationUsageStat.builder()
                            .applicationName(entry.getKey())
                            .usageDurationInMinutes(entry.getValue())
                            .usagePercentage(usagePercentage)
                            .build();
                })
                .collect(Collectors.toList());

        return new ApplicationUsageReport(start, end, totalActiveMinutes, totalIdleMinutes, stats);
    }


}
