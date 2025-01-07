package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.MonitorActivity;
import com.mongodb.client.model.Windows;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;

import javax.management.monitor.Monitor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiMonitorTracker {
    private static final User32 user32 = User32.INSTANCE;
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;

    public static Map<String, MonitorActivity> trackMultiMonitor() {
        Map<String, MonitorActivity> monitorActivity = new HashMap<>();
        AtomicInteger monitorIndex = new AtomicInteger(1);

        WinDef.HWND foregroundWindow = user32.GetForegroundWindow();
        int currentThreadId = kernel32.GetCurrentThreadId();

        user32.EnumDisplayMonitors(null, null, (hMonitor, hdc, rect, data) -> {
            String monitorId = "Monitor " + monitorIndex.getAndIncrement();
            WinDef.HWND topWindow = getTopWindowOnMonitor(rect);

            if (topWindow != null) {
                String windowTitle = getWindowTitle(topWindow);
                String applicationName = getApplicationName(topWindow);

                boolean isFocused = isWindowFocusedOnMonitor(topWindow, rect, foregroundWindow, currentThreadId);

                if (!windowTitle.isEmpty() && !isSystemWindow(windowTitle)) {
                    MonitorActivity activity = new MonitorActivity(windowTitle, applicationName, isFocused);
                    monitorActivity.put(monitorId, activity);
                }
            }
            return 1;
        }, null);

        return monitorActivity;
    }


    private static String getApplicationName(WinDef.HWND hwnd) {
        if (hwnd == null) {
            return "Unknown";
        }

        IntByReference processId = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, processId);

        WinNT.HANDLE processHandle = kernel32.OpenProcess(
                Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                false,
                processId.getValue()
        );
        if (processHandle == null) {
            return "Unknown";
        }

        try {
            char[] buffer = new char[1024];
            psapi.GetModuleFileNameExW(processHandle, null, buffer, buffer.length);
            String executablePath = Native.toString(buffer).trim();
            return extractApplicationName(executablePath);
        } finally {
            kernel32.CloseHandle(processHandle);
        }
    }

    private static String getWindowTitle(WinDef.HWND hwnd) {
        int length = user32.GetWindowTextLength(hwnd) + 1;
        char[] buffer = new char[length];
        user32.GetWindowText(hwnd, buffer, length);
        return new String(buffer).trim();
    }

    private static String extractApplicationName(String executablePath) {
        if (executablePath == null || executablePath.isEmpty()) {
            return "Unknown";
        }
        String fileName = executablePath.substring(executablePath.lastIndexOf('\\') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private static boolean isWindowFocusedOnMonitor(WinDef.HWND hwnd, WinUser.RECT monitorRect,
                                                    WinDef.HWND foregroundWindow,
                                                    int currentThreadId) {
        // Check if the window is on this monitor
        WinUser.RECT windowRect = new WinUser.RECT();
        user32.GetWindowRect(hwnd, windowRect);
        if (!isWindowOnMonitor(windowRect, monitorRect)) {
            return false;
        }

        // If this is the foreground window, it's definitely focused
        if (hwnd.equals(foregroundWindow)) {
            return true;
        }

        // Get the thread info for this window
        WinDef.DWORD windowThreadId = new WinDef.DWORD(
                user32.GetWindowThreadProcessId(hwnd, null)
        );

        // Attach to the input processing mechanism of the window's thread
        user32.AttachThreadInput(new WinDef.DWORD(currentThreadId),
                windowThreadId,
                true);

        try {
            // Get the window with focus in this thread
            WinDef.HWND focusedWindow = user32.GetActiveWindow();
            if (focusedWindow != null) {
                return focusedWindow.equals(hwnd);
            }
        } finally {
            // Always detach the thread input
            user32.AttachThreadInput(new WinDef.DWORD(currentThreadId),
                    windowThreadId,
                    false);
        }

        return false;
    }

    private static WinDef.HWND getTopWindowOnMonitor(WinUser.RECT monitorRect) {
        WinDef.HWND hwnd = user32.GetForegroundWindow();
        List<WindowInfo> windowsOnMonitor = new ArrayList<>();

        while (hwnd != null) {
            if (user32.IsWindowVisible(hwnd) && isTopLevelWindow(hwnd)) {
                WinUser.RECT windowRect = new WinUser.RECT();
                user32.GetWindowRect(hwnd, windowRect);

                if (isWindowOnMonitor(windowRect, monitorRect)) {
                    windowsOnMonitor.add(new WindowInfo(hwnd, getZOrder(hwnd)));
                }
            }
            hwnd = user32.GetWindow(hwnd, new WinDef.DWORD(WinUser.GW_HWNDNEXT));
        }

        return windowsOnMonitor.stream()
                .min(Comparator.comparingInt(WindowInfo::zOrder))
                .map(WindowInfo::hwnd)
                .orElse(null);
    }

    private static int getZOrder(WinDef.HWND hwnd) {
        int zOrder = 0;
        WinDef.HWND temp = hwnd;
        while ((temp = user32.GetWindow(temp, new WinDef.DWORD(WinUser.GW_HWNDPREV))) != null) {
            zOrder++;
        }
        return zOrder;
    }

    private static boolean isTopLevelWindow(WinDef.HWND hwnd) {
        WinDef.HWND owner = user32.GetWindow(hwnd, new WinDef.DWORD(WinUser.GW_OWNER));
        return owner == null;
    }

    private static boolean isWindowOnMonitor(WinUser.RECT windowRect, WinUser.RECT monitorRect) {
        int overlapLeft = Math.max(windowRect.left, monitorRect.left);
        int overlapTop = Math.max(windowRect.top, monitorRect.top);
        int overlapRight = Math.min(windowRect.right, monitorRect.right);
        int overlapBottom = Math.min(windowRect.bottom, monitorRect.bottom);

        int windowArea = (windowRect.right - windowRect.left) * (windowRect.bottom - windowRect.top);
        int overlapArea = Math.max(0, overlapRight - overlapLeft) * Math.max(0, overlapBottom - overlapTop);

        return ((double) overlapArea / windowArea) > 0.7;
    }

    private static boolean isSystemWindow(String windowTitle) {
        return windowTitle.equals("Program Manager") ||
                windowTitle.equals("Default IME") ||
                windowTitle.isEmpty();
    }

    private record WindowInfo(WinDef.HWND hwnd, int zOrder) {
    }
}
