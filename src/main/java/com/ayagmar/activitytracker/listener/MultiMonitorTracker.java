package com.ayagmar.activitytracker.listener;

import com.mongodb.client.model.Windows;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiMonitorTracker {
    private static final User32 user32 = User32.INSTANCE;

    public static Map<String, Map<String, String>> trackMultiMonitor() {
        Map<String, Map<String, String>> monitorActivity = new HashMap<>();
        AtomicInteger monitorIndex = new AtomicInteger(1);

        user32.EnumDisplayMonitors(null, null, (hMonitor, hdc, rect, data) -> {
            String monitorId = "Monitor " + monitorIndex.getAndIncrement();
            WinDef.HWND topWindow = getTopWindowOnMonitor(rect);

            if (topWindow != null) {
                String windowTitle = ActiveWindowTracker.getWindowTitle(topWindow);
                String applicationName = ActiveWindowTracker.getApplicationName(topWindow);

                if (!windowTitle.isEmpty() && !isSystemWindow(windowTitle)) {
                    Map<String, String> activity = new HashMap<>();
                    activity.put("activeWindowTitle", windowTitle);
                    activity.put("activeApplication", applicationName);
                    monitorActivity.put(monitorId, activity);
                }
            }
            return 1;
        }, null);

        return monitorActivity;
    }

    private static WinDef.HWND getTopWindowOnMonitor(WinUser.RECT monitorRect) {
        WinDef.HWND hwnd = user32.GetForegroundWindow();
        WinDef.HWND topWindow = null;
        int topZOrder = Integer.MAX_VALUE;

        while (hwnd != null) {
            if (user32.IsWindowVisible(hwnd) && isTopLevelWindow(hwnd)) {
                WinUser.RECT windowRect = new WinUser.RECT();
                user32.GetWindowRect(hwnd, windowRect);

                if (isWindowOnMonitor(windowRect, monitorRect)) {
                    int zOrder = getZOrder(hwnd);
                    if (zOrder < topZOrder) {
                        topZOrder = zOrder;
                        topWindow = hwnd;
                    }
                }
            }
            hwnd = user32.GetWindow(hwnd, new WinDef.DWORD(WinUser.GW_HWNDNEXT)); // Move to the next window
        }
        return topWindow;
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
        return windowTitle.equals("Program Manager") || windowTitle.equals("Default IME");
    }
}
