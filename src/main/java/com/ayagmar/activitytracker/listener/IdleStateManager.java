package com.ayagmar.activitytracker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
@Slf4j
public class IdleStateManager {
    @Value("${activity.idle.threshold-minutes:5}")
    private int idleThresholdMinutes;

    private volatile LocalDateTime lastActivityTime = LocalDateTime.now();
    private volatile LocalDateTime idleStartTime;
    private volatile boolean userIdle = false;

    public void updateActivity() {
        lastActivityTime = LocalDateTime.now();

        if (userIdle) {
            Duration idleDuration = Duration.between(idleStartTime, LocalDateTime.now());
            log.info("User returned from idle state after {} minutes", idleDuration.toMinutes());
            userIdle = false;
            idleStartTime = null;
        }
    }

    public boolean isIdle() {
        long idleMinutes = ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now());
        boolean currentlyIdle = idleMinutes >= idleThresholdMinutes;

        if (currentlyIdle && !userIdle) {
            userIdle = true;
            idleStartTime = LocalDateTime.now();
            log.info("User entered idle state");
        } else if (!currentlyIdle && userIdle) {
            Duration idleDuration = Duration.between(idleStartTime, LocalDateTime.now());
            log.info("User exited idle state after {} minutes", idleDuration.toMinutes());
            userIdle = false;
            idleStartTime = null;
        }

        return currentlyIdle;
    }


    public Optional<Duration> getCurrentIdleDuration() {
        if (!userIdle || idleStartTime == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(idleStartTime, LocalDateTime.now()));
    }


    public boolean isIdleLongerThan(Duration duration) {
        return getCurrentIdleDuration()
                .map(idleDuration -> idleDuration.compareTo(duration) > 0)
                .orElse(false);
    }
}