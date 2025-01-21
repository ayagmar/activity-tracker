package com.ayagmar.activitytracker.model;

import com.ayagmar.activitytracker.process.MonitorActivity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@Document(collection = "activity_logs")
@CompoundIndex(name = "timestamp_idle_idx", def = "{'timestamp': 1, 'isIdle': 1}")
public class ActivityLog {
    @Id
    private String id;
    @Setter
    @Indexed(expireAfter = "P30D")
    private LocalDateTime timestamp;
    private long leftClicks;
    private long rightClicks;
    private long middleClicks;
    private long keyPresses;
    private double mouseMovement;
    private boolean idle;
    private Map<String, MonitorActivity> monitorActivities;

    @Field("isIdle")
    public boolean isIdle() {
        return idle;
    }
}