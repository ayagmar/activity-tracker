package com.ayagmar.activitytracker.controller;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityTotals;
import com.ayagmar.activitytracker.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivityLogResource implements ActivityLogApi {

    private final ActivityLogService activityLogService;

    @Override
    public ResponseEntity<List<ActivityLog>> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return ResponseEntity.ok(activityLogService.getLogsByDateRange(start, end));
    }

    @Override
    public ResponseEntity<List<ActivityLog>> getAllLogs() {
        return ResponseEntity.ok(activityLogService.getAllLogs());
    }

    @Override
    public ResponseEntity<ActivityTotals> getActivityTotals(LocalDate date) {
        return ResponseEntity.ok(activityLogService.getActivityTotals(date));
    }
}
