package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;

public interface ActivityLogger {
    void logActivity(ActivityLog metrics);
}