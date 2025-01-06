package com.ayagmar.activitytracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableMongoRepositories(basePackages = "com.ayagmar.activitytracker.repository")
@EnableScheduling
public class MongoConfig {
}
