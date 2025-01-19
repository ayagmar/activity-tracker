package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityTotals;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final Clock clock;

    public List<ActivityLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end);
    }

    public ActivityTotals getActivityTotals(LocalDate date) {
        LocalDate targetDate = Optional.ofNullable(date)
                .orElseGet(() -> LocalDate.now(clock));

        DateTimeRange dateRange = DateTimeRange.ofFullDay(targetDate);

        List<ActivityLog> logs = repository.findByTimestampBetween(
                dateRange.getStart(),
                dateRange.getEnd()
        );

        return logs.stream()
                .reduce(
                        ActivityTotals.createEmptyTotals(targetDate),
                        this::accumulateActivityLog,
                        this::combineActivityTotals
                );
    }

    private ActivityTotals accumulateActivityLog(ActivityTotals totals, ActivityLog log) {
        return ActivityTotals.builder()
                .date(totals.getDate())
                .totalLeftClicks(totals.getTotalLeftClicks() + log.getLeftClicks())
                .totalRightClicks(totals.getTotalRightClicks() + log.getRightClicks())
                .totalMiddleClicks(totals.getTotalMiddleClicks() + log.getMiddleClicks())
                .totalKeyPresses(totals.getTotalKeyPresses() + log.getKeyPresses())
                .totalMouseMovement(totals.getTotalMouseMovement() + log.getMouseMovement())
                .build();
    }

    private ActivityTotals combineActivityTotals(ActivityTotals first, ActivityTotals second) {
        return ActivityTotals.builder()
                .date(first.getDate())
                .totalLeftClicks(first.getTotalLeftClicks() + second.getTotalLeftClicks())
                .totalRightClicks(first.getTotalRightClicks() + second.getTotalRightClicks())
                .totalMiddleClicks(first.getTotalMiddleClicks() + second.getTotalMiddleClicks())
                .totalKeyPresses(first.getTotalKeyPresses() + second.getTotalKeyPresses())
                .totalMouseMovement(first.getTotalMouseMovement() + second.getTotalMouseMovement())
                .build();
    }

    public List<ActivityLog> getAllLogs() {
        return repository.findAll();
    }


}
