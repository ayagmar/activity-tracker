package com.ayagmar.activitytracker.process;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class MonitorActivity {
    String windowTitle;
    @Setter
    String applicationName;
    boolean isFocused;
}