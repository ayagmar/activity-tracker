package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityTotals;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLog saveLog(ActivityLog log) {
        log.setTimestamp(LocalDateTime.now());
        return repository.save(log);
    }

    public List<ActivityLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end);
    }

    public ActivityTotals getActivityTotals(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<ActivityLog> logs = repository.findByTimestampBetween(startOfDay, endOfDay);
        long totalLeftClicks = 0;
        long totalRightClicks = 0;
        long totalMiddleClicks = 0;
        long totalKeyPresses = 0;
        double totalMouseMovement = 0.0;
        for (ActivityLog log : logs) {
            totalLeftClicks += log.getLeftClicks();
            totalRightClicks += log.getRightClicks();
            totalMiddleClicks += log.getMiddleClicks();
            totalKeyPresses += log.getKeyPresses();
            totalMouseMovement += log.getMouseMovement();
        }
        return ActivityTotals.builder()
                .date(date)
                .totalLeftClicks(totalLeftClicks)
                .totalRightClicks(totalRightClicks)
                .totalMiddleClicks(totalMiddleClicks)
                .totalKeyPresses(totalKeyPresses)
                .totalMouseMovement(totalMouseMovement)
                .build();
    }

    public List<ActivityLog> getAllLogs() {
        return repository.findAll();
    }

    public void deleteAllLogs() {
        this.repository.deleteAll();
    }

    public void afterPropertiesSet() {
        log.info("Deleting all logs");
        this.deleteAllLogs();
    }
}