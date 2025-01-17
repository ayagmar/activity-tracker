package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
public class WindowManager {
    private static final User32 user32 = User32.INSTANCE;

    public Optional<Window> getTopWindowOnMonitor(WinUser.RECT monitorRect) {
        return getVisibleWindowsOnMonitor(monitorRect).stream()
                .min(Comparator.comparingInt(Window::getZOrder))
                .filter(window -> !isSystemWindow(window.getTitle()));
    }

    private List<Window> getVisibleWindowsOnMonitor(WinUser.RECT monitorRect) {
        return StreamSupport.stream(
                        new WindowIterator(user32.GetForegroundWindow()).spliterator(),
                        false)
                .filter(this::isValidWindow)
                .filter(hwnd -> isWindowOnMonitor(getWindowRect(hwnd), monitorRect))
                .map(hwnd -> new Window(hwnd, getZOrder(hwnd)))
                .collect(Collectors.toList());
    }

    public boolean isWindowFocused(
            WinDef.HWND hwnd,
            WinUser.RECT monitorRect,
            WinDef.HWND foregroundWindow,
            int currentThreadId) {
        if (!isWindowOnMonitor(getWindowRect(hwnd), monitorRect)) {
            return false;
        }

        if (hwnd.equals(foregroundWindow)) {
            return true;
        }

        return checkThreadFocus(hwnd, currentThreadId);
    }

    private boolean checkThreadFocus(WinDef.HWND hwnd, int currentThreadId) {
        WinDef.DWORD windowThreadId = new WinDef.DWORD(
                user32.GetWindowThreadProcessId(hwnd, null)
        );

        if (!attachThreadInput(currentThreadId, windowThreadId.intValue(), true)) {
            return false;
        }

        try {
            WinDef.HWND focusedWindow = user32.GetActiveWindow();
            return focusedWindow != null && focusedWindow.equals(hwnd);
        } finally {
            attachThreadInput(currentThreadId, windowThreadId.intValue(), false);
        }
    }

    private boolean attachThreadInput(int currentThreadId, int windowThreadId, boolean attach) {
        return user32.AttachThreadInput(
                new WinDef.DWORD(currentThreadId),
                new WinDef.DWORD(windowThreadId),
                attach
        );
    }

    private boolean isValidWindow(WinDef.HWND hwnd) {
        return user32.IsWindowVisible(hwnd) && isTopLevelWindow(hwnd);
    }

    private boolean isTopLevelWindow(WinDef.HWND hwnd) {
        return user32.GetWindow(hwnd, new WinDef.DWORD(WinUser.GW_OWNER)) == null;
    }

    private WinUser.RECT getWindowRect(WinDef.HWND hwnd) {
        WinUser.RECT rect = new WinUser.RECT();
        user32.GetWindowRect(hwnd, rect);
        return rect;
    }

    private int getZOrder(WinDef.HWND hwnd) {
        int zOrder = 0;
        WinDef.HWND temp = hwnd;
        while ((temp = user32.GetWindow(temp, new WinDef.DWORD(WinUser.GW_HWNDPREV))) != null) {
            zOrder++;
        }
        return zOrder;
    }

    private boolean isWindowOnMonitor(WinUser.RECT windowRect, WinUser.RECT monitorRect) {
        Rectangle window = new Rectangle(
                windowRect.left,
                windowRect.top,
                windowRect.right - windowRect.left,
                windowRect.bottom - windowRect.top
        );

        Rectangle monitor = new Rectangle(
                monitorRect.left,
                monitorRect.top,
                monitorRect.right - monitorRect.left,
                monitorRect.bottom - monitorRect.top
        );

        Rectangle intersection = window.intersection(monitor);
        return !intersection.isEmpty() &&
                (intersection.getWidth() * intersection.getHeight()) /
                        (window.getWidth() * window.getHeight()) > 0.7;
    }

    private boolean isSystemWindow(String windowTitle) {
        return windowTitle.equals("Program Manager") ||
                windowTitle.equals("Default IME") ||
                windowTitle.isEmpty();
    }
}