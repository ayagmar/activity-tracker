package com.ayagmar.activitytracker.process;


public record ProcessInfo(String name) {
    public static ProcessInfo unknown() {
        return new ProcessInfo("Unknown");
    }
}