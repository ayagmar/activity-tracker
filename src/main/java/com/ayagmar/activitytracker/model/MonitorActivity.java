package com.ayagmar.activitytracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MonitorActivity {
    private String activeWindowTitle;
    private String activeApplication;
    private boolean isFocused;
}
