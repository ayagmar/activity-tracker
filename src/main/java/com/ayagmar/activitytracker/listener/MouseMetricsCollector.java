package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.config.DpiConfiguration;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MouseMetricsCollector implements NativeMouseInputListener {
    private static final double CENTIMETER_FACTOR = 2.54;
    private final MetricsHolder metricsHolder;
    private final IdleStateManager idleStateManager;
    private final MousePositionTracker mousePositionTracker;
    private final DpiConfiguration dpiConfiguration;
    private final NativeHookService nativeHookService;

    @PostConstruct
    public void initialize() {
        nativeHookService.addMouseListener(this);
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> metricsHolder.incrementLeftClick();
            case NativeMouseEvent.BUTTON2 -> metricsHolder.incrementMiddleClick();
            case NativeMouseEvent.BUTTON3 -> metricsHolder.incrementRightClick();
        }
        idleStateManager.updateActivity();
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        double distance = mousePositionTracker.trackMovement(e.getX(), e.getY());
        double centimeters = (distance / dpiConfiguration.getDpi()) * CENTIMETER_FACTOR;
        metricsHolder.addMouseMovement(centimeters);
        idleStateManager.updateActivity();
    }
}