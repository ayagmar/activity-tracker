package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.WinDef;

public interface ProcessInfoProvider {
    ProcessInfo getProcessInfo(WinDef.HWND hwnd);
}
