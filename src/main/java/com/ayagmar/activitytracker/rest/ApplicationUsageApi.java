package com.ayagmar.activitytracker.rest;

import com.ayagmar.activitytracker.model.ApplicationUsageReport;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@RequestMapping("/api/v1/application-usage")
@CrossOrigin
public interface ApplicationUsageApi {
    @GetMapping("/report")
    ResponseEntity<ApplicationUsageReport> getApplicationUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end);
}
