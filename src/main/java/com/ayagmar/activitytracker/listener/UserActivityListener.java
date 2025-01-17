package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityMetrics;
import com.ayagmar.activitytracker.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivityListener {
    private final MetricsCollector metricsCollector;
    private final IdleStateManager idleStateManager;
    private final ActivityLogService activityLogService;

    @Scheduled(fixedRateString = "${activity.logging.interval:1}", timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void logActivity() {
        ActivityMetrics metrics = metricsCollector.collectAndReset();
        boolean isIdle = idleStateManager.isIdle();

        ActivityLog activityLog = ActivityLog.builder()
                .leftClicks(metrics.getLeftClicks())
                .rightClicks(metrics.getRightClicks())
                .middleClicks(metrics.getMiddleClicks())
                .keyPresses(metrics.getKeyPresses())
                .mouseMovement(metrics.getMouseMovement())
                .monitorActivities(metrics.getMultiMonitorActivity())
                .isIdle(isIdle)
                .build();
        loggerActivity();
        activityLogService.saveLog(activityLog);
    }

    void loggerActivity() {
        MDC.put("date", LocalDateTime.now().toString());
        log.info("Recording activity metrics");
        MDC.clear();
    }
}