package com.ayagmar.activitytracker.listener;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

public class ActiveWindowTracker {
    private static final User32 user32 = User32.INSTANCE;
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;

    public static String getWindowTitle(WinDef.HWND hwnd) {
        if (hwnd == null) {
            return "Unknown";
        }
        char[] buffer = new char[1024];
        user32.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer).trim();
    }

    public static String getApplicationName(WinDef.HWND hwnd) {
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

    private static String extractApplicationName(String executablePath) {
        if (executablePath == null || executablePath.isEmpty()) {
            return "Unknown";
        }
        String fileName = executablePath.substring(executablePath.lastIndexOf('\\') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
