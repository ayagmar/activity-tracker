package com.ayagmar.activitytracker.process;

import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class MonitorActivity {
    String windowTitle;
    String applicationName;
    boolean isFocused;
}