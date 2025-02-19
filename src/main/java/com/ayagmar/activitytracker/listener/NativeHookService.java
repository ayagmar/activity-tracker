package com.ayagmar.activitytracker.listener;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Slf4j
public class NativeHookService {
    @PostConstruct
    public void initialize() throws Exception {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        if (!GlobalScreen.isNativeHookRegistered()) {
            log.info("Registering native hook");
            GlobalScreen.registerNativeHook();
        }
    }

    public void addKeyListener(NativeKeyListener listener) {
        GlobalScreen.addNativeKeyListener(listener);
    }

    public void addMouseListener(NativeMouseInputListener listener) {
        GlobalScreen.addNativeMouseListener(listener);
        GlobalScreen.addNativeMouseMotionListener(listener);
    }

    @PreDestroy
    public void destroy() {
        if (GlobalScreen.isNativeHookRegistered()) {
            try {
                log.info("Removing native hook");
                GlobalScreen.unregisterNativeHook();
            } catch (Exception e) {
                log.error("Error unregistering native hook: {}", e.getMessage());
            }
        }
    }
}