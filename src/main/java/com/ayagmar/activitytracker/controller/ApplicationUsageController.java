package com.ayagmar.activitytracker.controller;

import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.model.ApplicationUsageStat;
import com.ayagmar.activitytracker.service.ApplicationUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicationUsageController implements ApplicationUsageApi {

    private final ApplicationUsageService applicationUsageService;

    @Override
    public ResponseEntity<ApplicationUsageReport> getApplicationUsage(LocalDateTime start, LocalDateTime end) {
        return ResponseEntity.ok(applicationUsageService.calculateApplicationUsage(start, end));
    }
}
