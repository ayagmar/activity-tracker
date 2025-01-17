package com.ayagmar.activitytracker.rest;

import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import com.ayagmar.activitytracker.service.ActivityUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class ApplicationUsageController implements ApplicationUsageApi {

    private final ActivityUsageService activityUsageService;

    @Override
    public ResponseEntity<ApplicationUsageReport> getApplicationUsage(LocalDateTime start, LocalDateTime end) {
        return ResponseEntity.ok(activityUsageService.calculateApplicationUsage(start, end));
    }
}
