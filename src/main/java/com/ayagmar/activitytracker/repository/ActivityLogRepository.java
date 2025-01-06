package com.ayagmar.activitytracker.repository;

import com.ayagmar.activitytracker.model.ActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
