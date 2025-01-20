package com.ayagmar.activitytracker.listener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyboardMetricsCollector implements NativeKeyListener {
    private final MetricsHolder metricsHolder;
    private final IdleStateManager idleStateManager;
    private final NativeHookService nativeHookService;

    @PostConstruct
    public void initialize() {
        nativeHookService.addKeyListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        metricsHolder.incrementKeyPress();
        idleStateManager.updateActivity();
    }
}
