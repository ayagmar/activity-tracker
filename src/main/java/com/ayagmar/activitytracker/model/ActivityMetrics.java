package com.ayagmar.activitytracker.model;

import com.ayagmar.activitytracker.process.MonitorActivity;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ActivityMetrics {
    long leftClicks;
    long rightClicks;
    long middleClicks;
    long keyPresses;
    double mouseMovement;
    boolean isIdle;
    Map<String, MonitorActivity> multiMonitorActivity;
}