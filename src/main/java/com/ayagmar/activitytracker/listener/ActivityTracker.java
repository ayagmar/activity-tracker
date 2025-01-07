package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityMetrics;
import com.ayagmar.activitytracker.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityTracker {
    private final MetricsCollector metricsCollector;
    private final IdleStateManager idleStateManager;
    private final ActivityLogService activityLogService;

    @Scheduled(fixedRateString = "${activity.logging.interval:1}", timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void logActivity() {
        log.info("Recording activity metrics");
        ActivityMetrics metrics = metricsCollector.collectAndReset();
        boolean isIdle = idleStateManager.isIdle();

        ActivityLog activityLog = ActivityLog.builder()
                .leftClicks(metrics.getLeftClicks())
                .rightClicks(metrics.getRightClicks())
                .middleClicks(metrics.getMiddleClicks())
                .keyPresses(metrics.getKeyPresses())
                .mouseMovement(metrics.getMouseMovement())
                .activity(MultiMonitorTracker.trackMultiMonitor())
                .isIdle(isIdle)
                .build();

        activityLogService.saveLog(activityLog);
    }
}