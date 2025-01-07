package com.ayagmar.activitytracker.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MousePositionTracker {
    private int lastX = -1;
    private int lastY = -1;

    public double trackMovement(int newX, int newY) {
        if (lastX == -1 || lastY == -1) {
            lastX = newX;
            lastY = newY;
            return 0.0;
        }

        double distance = Math.hypot(newX - lastX, newY - lastY);
        lastX = newX;
        lastY = newY;
        return distance;
    }
}