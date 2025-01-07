package com.ayagmar.activitytracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public record MonitorActivity(String activeWindowTitle, String activeApplication, boolean focused) {
}
