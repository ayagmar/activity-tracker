package com.ayagmar.activitytracker.process;

import com.ayagmar.activitytracker.model.ApplicationType;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorTracker {
    private final WindowManager windowManager;
    private final ProcessTracker processTracker;

    public Map<String, MonitorActivity> trackAllMonitors() {
        Map<String, MonitorActivity> monitorActivity = new HashMap<>();
        AtomicInteger monitorIndex = new AtomicInteger(1);

        WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
        int currentThreadId = Kernel32.INSTANCE.GetCurrentThreadId();

        User32.INSTANCE.EnumDisplayMonitors(null, null,
                (hMonitor, hdc, rect, data) -> {
                    String monitorId = String.format("Monitor-%d", monitorIndex.getAndIncrement());
                    trackMonitorActivity(rect, foregroundWindow, currentThreadId)
                            .ifPresent(activity -> monitorActivity.put(monitorId, activity));
                    return 1;
                }, null);

        return monitorActivity;
    }

    private Optional<MonitorActivity> trackMonitorActivity(
            WinUser.RECT monitorRect,
            WinDef.HWND foregroundWindow,
            int currentThreadId) {
        return windowManager.getTopWindowOnMonitor(monitorRect)
                .map(window -> createMonitorActivity(window, monitorRect, foregroundWindow, currentThreadId));
    }

    private MonitorActivity createMonitorActivity(
            Window window,
            WinUser.RECT monitorRect,
            WinDef.HWND foregroundWindow,
            int currentThreadId) {
        String windowTitle = window.getTitle();
        ProcessInfo processInfo = processTracker.getProcessInfo(window.getHwnd());
        boolean isFocused = windowManager.isWindowFocused(
                window.getHwnd(),
                monitorRect,
                foregroundWindow,
                currentThreadId
        );
        ApplicationType applicationType = ApplicationType.fromProcessName(processInfo.name());
        String appName = Optional.ofNullable(applicationType).map(ApplicationType::displayName).orElse(processInfo.name());
        return MonitorActivity.builder()
                .windowTitle(windowTitle)
                .applicationName(appName)
                .isFocused(isFocused)
                .build();
    }
}