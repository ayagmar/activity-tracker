package com.ayagmar.activitytracker.listener;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MongoActivityLogger implements ActivityLogger {
    private final ActivityLogRepository repository;

    @Override
    public void logActivity(ActivityLog log) {
        log.setTimestamp(LocalDateTime.now());
        repository.save(log);
    }
}
