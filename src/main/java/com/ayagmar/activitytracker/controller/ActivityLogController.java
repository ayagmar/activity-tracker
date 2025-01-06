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
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;

    @PostMapping("/log")
    public ResponseEntity<ActivityLog> logActivity(@RequestBody ActivityLog log) {
        return ResponseEntity.ok(service.saveLog(log));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActivityLog>> getLogsByDateRange(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return ResponseEntity.ok(service.getLogsByDateRange(startTime, endTime));
    }

    @GetMapping("/list")
    public ResponseEntity<List<ActivityLog>> getAllLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }

    @GetMapping("/totals")
    public ActivityTotals getActivityTotals(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getActivityTotals(date);
    }
}
