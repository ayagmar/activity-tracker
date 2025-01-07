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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalActivityListener implements NativeKeyListener, NativeMouseInputListener, InitializingBean {
    public static final double CENTIMETER_FACTOR = 2.54;
    public static final double DPI = 91.79; // Could be configurable if needed
    private static final int IDLE_THRESHOLD_MINUTES = 5;

    private final ActivityLogService activityLogService;

    private final AtomicLong leftClicks = new AtomicLong();
    private final AtomicLong rightClicks = new AtomicLong();
    private final AtomicLong middleClicks = new AtomicLong();
    private final AtomicLong keyPresses = new AtomicLong();
    private final DoubleAdder mouseMovement = new DoubleAdder();

    // Simplified: Use volatile for thread safety since LocalDateTime is immutable
    private volatile LocalDateTime lastActivityTime = LocalDateTime.now();
    private volatile LocalDateTime idleStartTime;

    private volatile boolean userIdle = false;
    private int lastX = -1;
    private int lastY = -1;

    @Override
    public void afterPropertiesSet() throws Exception {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        if (!GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.registerNativeHook();
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        keyPresses.incrementAndGet();
        updateActivityTime();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        switch (e.getButton()) {
            case NativeMouseEvent.BUTTON1 -> leftClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON2 -> rightClicks.incrementAndGet();
            case NativeMouseEvent.BUTTON3 -> middleClicks.incrementAndGet();
        }
        updateActivityTime();
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
        updateActivityTime();
    }

    private double pixelsToCentimeters(double pixels) {
        double inches = pixels / DPI;
        return inches * CENTIMETER_FACTOR;
    }

    @Scheduled(fixedRateString = "1", timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void saveActivityLog() {
        log.info("saving activity log");
        LocalDateTime now = LocalDateTime.now();
        boolean isIdle = isUserIdle();

        long idleDuration = 0;
        if (isIdle && idleStartTime == null) {
            idleStartTime = now;
        } else if (!isIdle && idleStartTime != null) {
            idleDuration = ChronoUnit.SECONDS.between(idleStartTime, now);
            idleStartTime = null;
        }

        ActivityLog log = ActivityLog.builder()
                .leftClicks(leftClicks.get())
                .rightClicks(rightClicks.get())
                .middleClicks(middleClicks.get())
                .keyPresses(keyPresses.get())
                .mouseMovement(mouseMovement.doubleValue())
                .activity(MultiMonitorTracker.trackMultiMonitor())
                .isIdle(isIdle)
                .idleDuration(idleDuration)
                .build();

        activityLogService.saveLog(log);

        // Reset counters only after log is saved to avoid resetting during an active log cycle
        resetCounters();
    }

    public void resetCounters() {
        leftClicks.set(0);
        rightClicks.set(0);
        middleClicks.set(0);
        keyPresses.set(0);
        mouseMovement.reset();
    }

    private boolean isUserIdle() {
        long idleMinutes = ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now());
        boolean currentlyIdle = idleMinutes >= IDLE_THRESHOLD_MINUTES;

        if (currentlyIdle && !userIdle) {
            userIdle = true;
            idleStartTime = LocalDateTime.now();
        } else if (!currentlyIdle && userIdle) {
            userIdle = false;
            idleStartTime = null;
        }

        return currentlyIdle;
    }

    private void updateActivityTime() {
        lastActivityTime = LocalDateTime.now();

        if (userIdle) {
            userIdle = false;
            idleStartTime = null;
        }
    }

}
