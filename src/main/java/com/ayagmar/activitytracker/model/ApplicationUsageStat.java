package com.ayagmar.activitytracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class ApplicationUsageStat {
    private String applicationName;
    private long usageDurationInMinutes;
    @Setter
    private double usagePercentage;
}
