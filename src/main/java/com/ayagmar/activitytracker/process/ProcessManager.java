package com.ayagmar.activitytracker.process;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class ProcessManager {
    private static final Kernel32 kernel32 = Kernel32.INSTANCE;
    private static final Psapi psapi = Psapi.INSTANCE;
    private static final User32 user32 = User32.INSTANCE;

    public ProcessInfo getProcessInfo(WinDef.HWND hwnd) {
        IntByReference processId = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, processId);

        try (ProcessHandle handle = openProcess(processId.getValue()).orElse(null)) {
            if (handle != null) {
                return getProcessName(handle);
            }
        } catch (Exception e) {
            log.error("Error retrieving process info: {}", e.getMessage());
        }

        return ProcessInfo.unknown();
    }

    private Optional<ProcessHandle> openProcess(int processId) {
        WinNT.HANDLE handle = kernel32.OpenProcess(
                Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ,
                false,
                processId
        );

        return Optional.ofNullable(handle)
                .map(h -> new ProcessHandle(h, kernel32));
    }

    private ProcessInfo getProcessName(ProcessHandle handle) {
        char[] buffer = new char[1024];
        psapi.GetModuleFileNameExW(handle.getHandle(), null, buffer, buffer.length);
        String executablePath = Native.toString(buffer).trim();
        return new ProcessInfo(extractApplicationName(executablePath));
    }

    private String extractApplicationName(String executablePath) {
        if (executablePath == null || executablePath.isEmpty()) {
            return "Unknown";
        }
        String fileName = executablePath.substring(executablePath.lastIndexOf('\\') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
