package com.ayagmar.activitytracker.controller;

import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.service.ApplicationUsageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/usage")
public class ApplicationUsageController {
    private final ApplicationUsageService usageService;

    public ApplicationUsageController(ApplicationUsageService usageService) {
        this.usageService = usageService;
    }
    @GetMapping("/calculate")
    public ApplicationUsageReport getApplicationUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return usageService.calculateApplicationUsage(start, end);
    }
}
