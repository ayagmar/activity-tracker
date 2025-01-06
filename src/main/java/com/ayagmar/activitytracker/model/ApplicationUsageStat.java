package com.ayagmar.activitytracker.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationUsageStat {
    private String applicationName;
    private long usageDurationInMinutes;
    private double usagePercentage;
}
