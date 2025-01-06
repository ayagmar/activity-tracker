package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.service.ActivityLogService;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalActivityListener implements NativeKeyListener, NativeMouseInputListener, InitializingBean {
    public static final double CENTIMER_FACTOR = 2.54;
    public static final double DPI = 91.79;
    private final ActivityLogService activityLogService;
    private final AtomicLong leftClicks = new AtomicLong();
    private final AtomicLong rightClicks = new AtomicLong();
    private final AtomicLong middleClicks = new AtomicLong();
    private final AtomicLong keyPresses = new AtomicLong();
    private final DoubleAdder mouseMovement  = new DoubleAdder();
    private final AtomicReference<String> activeApplication = new AtomicReference<>("Unknown");
    private final AtomicReference<String> activeWindowTitle = new AtomicReference<>("Unknown");
    private int lastX = -1;
    private int lastY = -1;


    @Override
    public void afterPropertiesSet() throws Exception {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        log.info("Key pressed: {}", e.getKeyCode());
        keyPresses.incrementAndGet();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        log.info("Native Mouse Pressed: {}", e.getButton());
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> leftClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON2 -> rightClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON3 -> middleClicks.incrementAndGet();
        }
        log.info("right clicks: {}", rightClicks.get());
        log.info("left clicks: {}", leftClicks.get());
    }


    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        if (lastX != -1 && lastY != -1) {
            double distancePixels = Math.hypot(e.getX() - lastX, e.getY() - lastY);
            double distanceCm = pixelsToCentimeters(distancePixels);
            mouseMovement.add(distanceCm);
        }
        lastX = e.getX();
        lastY = e.getY();
    }

    private void updateActiveApplicationAndWindow() {
        activeApplication.set(WindowsFocusTracker.getActiveApplication());
        activeWindowTitle.set(WindowsFocusTracker.getActiveWindowTitle());
    }

    private double pixelsToCentimeters(double pixels) {
        double inches = pixels / DPI;
        return inches * CENTIMER_FACTOR;
    }

    @Scheduled(fixedRateString = "1", timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void saveActivityLog() {
        log.info("Saving activity log ");
        this.updateActiveApplicationAndWindow();
        ActivityLog log = ActivityLog.builder()
                .leftClicks(leftClicks.get())
                .rightClicks(rightClicks.get())
                .middleClicks(middleClicks.get())
                .keyPresses(keyPresses.get())
                .mouseMovement(mouseMovement.doubleValue())
                .activeApplication(activeApplication.get())
                .activeWindowTitle(activeWindowTitle.get())
                .build();
        activityLogService.saveLog(log);
        this.resetCounters();
    }

    public void resetCounters() {
        leftClicks.set(0);
        rightClicks.set(0);
        middleClicks.set(0);
        keyPresses.set(0);
        mouseMovement.reset();
        activeApplication.set("Unknown");
        activeWindowTitle.set("Unknown");
    }
}
