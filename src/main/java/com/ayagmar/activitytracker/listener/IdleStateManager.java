package com.ayagmar.activitytracker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class IdleStateManager {
    private final Clock clock;
    private final int idleThresholdMinutes;

    private volatile LocalDateTime lastActivityTime;
    private volatile LocalDateTime idleStartTime;
    private volatile boolean userIdle = false;

    public IdleStateManager(
            @Value("${activity.idle.threshold-minutes:5}") int idleThresholdMinutes,
            Clock clock) {
        this.idleThresholdMinutes = idleThresholdMinutes;
        this.clock = clock;
        this.lastActivityTime = LocalDateTime.now(clock);
    }

    public void updateActivity() {
        lastActivityTime = LocalDateTime.now(clock);
        if (userIdle) {
            Duration idleDuration = Duration.between(idleStartTime, LocalDateTime.now(clock));
            log.info("User returned from idle state after {} minutes", idleDuration.toMinutes());
            userIdle = false;
            idleStartTime = null;
        }
    }

    public boolean checkAndSetIdleStatus() {
        LocalDateTime now = LocalDateTime.now(clock);
        long idleMinutes = ChronoUnit.MINUTES.between(lastActivityTime, now);
        boolean currentlyIdle = idleMinutes >= idleThresholdMinutes;

        if (currentlyIdle && !userIdle) {
            userIdle = true;
            idleStartTime = now;
            log.info("User entered idle state");
        } else if (!currentlyIdle && userIdle) {
            Duration idleDuration = Duration.between(idleStartTime, now);
            log.info("User exited idle state after {} minutes", idleDuration.toMinutes());
            userIdle = false;
            idleStartTime = null;
        }

        return currentlyIdle;
    }

}