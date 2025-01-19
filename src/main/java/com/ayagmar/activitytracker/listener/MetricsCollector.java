package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.config.MetricsConfiguration;
import com.ayagmar.activitytracker.model.ActivityMetrics;
import com.ayagmar.activitytracker.process.MonitorTracker;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsCollector implements NativeKeyListener, NativeMouseInputListener {
    private static final double CENTIMETER_FACTOR = 2.54;
    private final MetricsConfiguration metricsConfiguration;
    private final NativeHookService nativeHookService;
    private final IdleStateManager idleStateManager;
    private final MousePositionTracker mousePositionTracker;
    private final MonitorTracker monitorTracker;
    private final AtomicLong leftClicks = new AtomicLong();
    private final AtomicLong rightClicks = new AtomicLong();
    private final AtomicLong middleClicks = new AtomicLong();
    private final AtomicLong keyPresses = new AtomicLong();
    private final DoubleAdder mouseMovement = new DoubleAdder();


    @PostConstruct
    public void initialize() {
        nativeHookService.addKeyListener(this);
        nativeHookService.addMouseListener(this);
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        keyPresses.incrementAndGet();
        idleStateManager.updateActivity();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> leftClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON2 -> middleClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON3 -> rightClicks.incrementAndGet();
        }
        idleStateManager.updateActivity();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        double distance = mousePositionTracker.trackMovement(e.getX(), e.getY());
        mouseMovement.add(convertToMetric(distance));
        idleStateManager.updateActivity();
    }

    private double convertToMetric(double pixels) {
        return (pixels / metricsConfiguration.getDpi()) * CENTIMETER_FACTOR;
    }

    public ActivityMetrics collectAndReset() {
        ActivityMetrics metrics = ActivityMetrics.builder()
                .leftClicks(leftClicks.get())
                .rightClicks(rightClicks.get())
                .middleClicks(middleClicks.get())
                .keyPresses(keyPresses.get())
                .mouseMovement(mouseMovement.doubleValue())
                .multiMonitorActivity(monitorTracker.trackAllMonitors())
                .isIdle(idleStateManager.isIdle())
                .build();

        resetMetrics();
        return metrics;
    }

    private void resetMetrics() {
        leftClicks.set(0);
        rightClicks.set(0);
        middleClicks.set(0);
        keyPresses.set(0);
        mouseMovement.reset();
    }
}