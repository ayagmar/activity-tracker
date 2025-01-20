package com.ayagmar.activitytracker.model;

import java.util.Set;


public enum ApplicationType {
    BROWSER(
            "Web Browser",
            "General",
            Set.of("librewolf", "msedge", "chrome", "firefox"),
            true
    ),
    IDE(
            "Intellij Idea",
            "Coding",
            Set.of("idea64"),
            true
    ),
    API_CLIENT(
            "API Client",
            "Coding",
            Set.of("postman"),
            true
    ),
    COMMUNICATION(
            "Discord",
            "Communication",
            Set.of("discord"),
            true
    ),
    GAMING(
            "Valorant",
            "Gaming",
            Set.of("valorant-win64-shipping"),
            false
    ),
    MMO(
            "Final Fantasy XIV",
            "Gaming",
            Set.of("ffxiv_dx11"),
            false
    ),
    RIVALS(
            "Marvel Rivals",
            "Gaming",
            Set.of("marvel-win64-shipping"),
            false
    );

    private final String displayName;
    private final String category;
    private final Set<String> processNames;
    private final boolean allowsMultipleInstances;

    ApplicationType(String displayName, String category,
                    Set<String> processNames, boolean allowsMultipleInstances) {
        this.displayName = displayName;
        this.category = category;
        this.processNames = processNames;
        this.allowsMultipleInstances = allowsMultipleInstances;
    }

    public static ApplicationType fromProcessName(String processName) {
        String lowerName = processName.toLowerCase();
        for (ApplicationType type : values()) {
            if (type.processNames.contains(lowerName)) {
                return type;
            }
        }
        return null;
    }

    // Getters
    public String displayName() {
        return displayName;
    }

    public String category() {
        return category;
    }

    public Set<String> processNames() {
        return processNames;
    }

    public boolean allowsMultipleInstances() {
        return allowsMultipleInstances;
    }
}