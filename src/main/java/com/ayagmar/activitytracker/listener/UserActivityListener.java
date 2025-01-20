package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityMetrics;
import com.ayagmar.activitytracker.process.MonitorActivity;
import com.ayagmar.activitytracker.process.MonitorTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivityListener {
    private final MetricsHolder metricsHolder;
    private final IdleStateManager idleStateManager;
    private final ActivityLogger activityLogger;
    private final MonitorTracker monitorTracker;

    @Scheduled(fixedRateString = "${activity.logging.interval:1}", timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void logActivity() {
        ActivityMetrics metrics = metricsHolder.collectAndReset();
        boolean isIdle = idleStateManager.checkAndSetIdleStatus();
        Map<String, MonitorActivity> monitorActivities = monitorTracker.trackAllMonitors();

        ActivityLog activityLog = ActivityLog.builder()
                .leftClicks(metrics.getLeftClicks())
                .rightClicks(metrics.getRightClicks())
                .middleClicks(metrics.getMiddleClicks())
                .keyPresses(metrics.getKeyPresses())
                .mouseMovement(metrics.getMouseMovement())
                .monitorActivities(monitorActivities)
                .isIdle(isIdle)
                .build();

        log.info("Recording activity metrics");
        activityLogger.logActivity(activityLog);
    }

}