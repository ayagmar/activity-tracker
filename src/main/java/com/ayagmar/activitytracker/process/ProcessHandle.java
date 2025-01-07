package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessHandle implements AutoCloseable {
    @Getter
    private final WinNT.HANDLE handle;
    private final Kernel32 kernel32;

    @Override
    public void close() {
        kernel32.CloseHandle(handle);
    }
}