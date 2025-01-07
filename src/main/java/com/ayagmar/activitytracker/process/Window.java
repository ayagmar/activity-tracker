package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.Value;

@Value
public class Window {
    WinDef.HWND hwnd;
    int zOrder;

    public String getTitle() {
        int length = User32.INSTANCE.GetWindowTextLength(hwnd) + 1;
        char[] buffer = new char[length];
        User32.INSTANCE.GetWindowText(hwnd, buffer, length);
        return new String(buffer).trim();
    }
}