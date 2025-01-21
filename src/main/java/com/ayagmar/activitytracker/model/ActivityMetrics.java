package com.ayagmar.activitytracker.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ActivityMetrics {
    long leftClicks;
    long rightClicks;
    long middleClicks;
    long keyPresses;
    double mouseMovement;
}