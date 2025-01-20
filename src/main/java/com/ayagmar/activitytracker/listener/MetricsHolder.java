package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityMetrics;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Component
public class MetricsHolder {
    private final AtomicLong leftClicks = new AtomicLong();
    private final AtomicLong rightClicks = new AtomicLong();
    private final AtomicLong middleClicks = new AtomicLong();
    private final AtomicLong keyPresses = new AtomicLong();
    private final DoubleAdder mouseMovement = new DoubleAdder();

    public void incrementLeftClick() {
        leftClicks.incrementAndGet();
    }

    public void incrementRightClick() {
        rightClicks.incrementAndGet();
    }

    public void incrementMiddleClick() {
        middleClicks.incrementAndGet();
    }

    public void incrementKeyPress() {
        keyPresses.incrementAndGet();
    }

    public void addMouseMovement(double distance) {
        mouseMovement.add(distance);
    }

    public ActivityMetrics collectAndReset() {
        return ActivityMetrics.builder()
                .leftClicks(leftClicks.getAndSet(0))
                .rightClicks(rightClicks.getAndSet(0))
                .middleClicks(middleClicks.getAndSet(0))
                .keyPresses(keyPresses.getAndSet(0))
                .mouseMovement(mouseMovement.sumThenReset())
                .build();
    }
}