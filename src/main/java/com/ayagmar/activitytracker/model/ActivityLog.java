package com.ayagmar.activitytracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@Document(collection = "activity_logs")
public class ActivityLog {
    @Id
    private String id;
    @Setter
    private LocalDateTime timestamp;
    private long leftClicks;
    private long rightClicks;
    private long middleClicks;
    private long keyPresses;
    private double mouseMovement;
    private boolean isIdle;
    private long idleDuration;
    private Map<String, MonitorActivity> activity;
}