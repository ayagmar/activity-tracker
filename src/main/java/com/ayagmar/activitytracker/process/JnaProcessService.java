package com.ayagmar.activitytracker.process;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinNT;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JnaProcessService {
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;

    public Optional<ProcessHandle> openProcess(int processId) {
        WinNT.HANDLE handle = kernel32.OpenProcess(
                Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                false,
                processId
        );

        return Optional.ofNullable(handle)
                .map(h -> new ProcessHandle(h, kernel32));
    }

    public String getProcessName(WinNT.HANDLE handle) {
        char[] buffer = new char[260];
        int result = psapi.GetModuleFileNameExW(handle, null, buffer, buffer.length);
        if (result == 0) {
            return "";
        }
        return Native.toString(buffer).trim();
    }
}
