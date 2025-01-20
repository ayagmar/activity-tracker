package com.ayagmar.activitytracker.util;

import com.ayagmar.activitytracker.config.DpiConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DistanceConverter {
    private static final double CM_PER_INCH = 2.54;
    private final DpiConfiguration dpiConfig;

    public double pixelsToCentimeters(double pixels) {
        return (pixels / dpiConfig.getDpi()) * CM_PER_INCH;
    }
}