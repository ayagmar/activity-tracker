package com.ayagmar.activitytracker.process;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessTracker implements ProcessInfoProvider {
    private final JnaProcessService processService;

    public ProcessInfo getProcessInfo(WinDef.HWND hwnd) {
        IntByReference processId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, processId);
        try (ProcessHandle handle = processService.openProcess(processId.getValue()).orElse(null)) {
            if (handle != null) {
                String processName = processService.getProcessName(handle.getHandle());
                return new ProcessInfo(extractApplicationName(processName));
            }
        } catch (Exception e) {
            log.error("Error retrieving process info: {}", e.getMessage());
        }
        return ProcessInfo.unknown();
    }

    private String extractApplicationName(String executablePath) {
        if (executablePath == null || executablePath.isEmpty()) {
            return "Unknown";
        }
        String fileName = executablePath.substring(executablePath.lastIndexOf('\\') + 1);
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}

