package com.ayagmar.activitytracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "activity.metrics")
@Getter
@Setter
public class DpiConfiguration {
    private double dpi = 0.0;
}
