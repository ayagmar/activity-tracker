package com.ayagmar.activitytracker.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class IdleStateManager {
    private final Clock clock;
    private final int idleThresholdMinutes;

    private final AtomicReference<LocalDateTime> lastActivityTime = new AtomicReference<>();
    private final AtomicReference<LocalDateTime> idleStartTime = new AtomicReference<>();
    private final AtomicBoolean userIdle = new AtomicBoolean(false);

    public IdleStateManager(
            @Value("${activity.idle.threshold-minutes:5}") int idleThresholdMinutes,
            Clock clock) {
        this.idleThresholdMinutes = idleThresholdMinutes;
        this.clock = clock;
        this.lastActivityTime.set(LocalDateTime.now(clock));
    }

    public void updateActivity() {
        lastActivityTime.set(LocalDateTime.now(clock));
        if (userIdle.getAndSet(false)) {
            Duration idleDuration = Duration.between(idleStartTime.getAndSet(null), LocalDateTime.now(clock));
            log.info("User returned from idle state after {} minutes", idleDuration.toMinutes());
        }
    }

    public boolean checkAndSetIdleStatus() {
        LocalDateTime now = LocalDateTime.now(clock);
        long idleMinutes = ChronoUnit.MINUTES.between(lastActivityTime.get(), now);
        boolean currentlyIdle = idleMinutes >= idleThresholdMinutes;

        if (userIdle.compareAndSet(false, currentlyIdle) && currentlyIdle) {
            idleStartTime.set(now);
            log.info("User entered idle state");
        } else if (userIdle.compareAndSet(true, currentlyIdle) && !currentlyIdle) {
            Duration idleDuration = Duration.between(idleStartTime.getAndSet(null), now);
            log.info("User exited idle state after {} minutes", idleDuration.toMinutes());
        }
        return currentlyIdle;
    }
}
