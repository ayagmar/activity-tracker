package com.ayagmar.activitytracker.model;

import java.util.Set;


public record ApplicationType(String displayName, String category, Set<String> processNames,
                              boolean allowsMultipleInstances) {
    public static final ApplicationType BROWSER = new ApplicationType(
            "Web Browser",
            "General",
            Set.of("librewolf", "msedge", "chrome", "firefox"),
            true
    );

    public static final ApplicationType IDE = new ApplicationType(
            "Intellij Idea",
            "Coding",
            Set.of("idea64"),
            true
    );

    public static final ApplicationType API_CLIENT = new ApplicationType(
            "API Client",
            "Coding",
            Set.of("postman"),
            true
    );

    public static final ApplicationType COMMUNICATION = new ApplicationType(
            "Discord",
            "Communication",
            Set.of("discord"),
            true
    );

    public static final ApplicationType GAMING = new ApplicationType(
            "Valorant",
            "Gaming",
            Set.of("valorant-win64-shipping"),
            false
    );

    public static final ApplicationType MMO = new ApplicationType(
            "Final Fantasy XIV",
            "Gaming",
            Set.of("ffxiv_dx11"),
            false
    );

    public static final ApplicationType RIVALS = new ApplicationType(
            "Marvel Rivals",
            "Gaming",
            Set.of("marvel-win64-shipping"),
            false
    );

    private static final Set<ApplicationType> REGISTRY = Set.of(
            BROWSER, IDE, API_CLIENT, COMMUNICATION, GAMING, MMO, RIVALS
    );

    public static ApplicationType fromProcessName(String processName) {
        return REGISTRY.stream()
                .filter(applicationType -> applicationType.matchesProcess(processName))
                .findFirst().orElse(null);
    }

    boolean matchesProcess(String processName) {
        return processNames.contains(processName.toLowerCase());
    }
}