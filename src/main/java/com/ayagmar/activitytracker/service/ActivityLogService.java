package com.ayagmar.activitytracker.service;

import com.ayagmar.activitytracker.model.ActivityLog;
import com.ayagmar.activitytracker.model.ActivityTotals;
import com.ayagmar.activitytracker.util.DateTimeRange;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final MongoTemplate mongoTemplate;
    private final Clock clock;

    public List<ActivityLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        Criteria criteria = Criteria.where("timestamp")
                .gte(start)
                .lte(end);

        Query query = Query.query(criteria);
        return mongoTemplate.find(query, ActivityLog.class);
    }

    public ActivityTotals getActivityTotals(LocalDate date) {
        LocalDate targetDate = Optional.ofNullable(date)
                .orElseGet(() -> LocalDate.now(clock));

        DateTimeRange dateRange = DateTimeRange.ofFullDay(targetDate);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("timestamp")
                                .gte(dateRange.getStart())
                                .lte(dateRange.getEnd())
                ),
                Aggregation.group()
                        .sum("leftClicks").as("totalLeftClicks")
                        .sum("rightClicks").as("totalRightClicks")
                        .sum("middleClicks").as("totalMiddleClicks")
                        .sum("keyPresses").as("totalKeyPresses")
                        .sum("mouseMovement").as("totalMouseMovement")
                        .first("timestamp").as("timestamp")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                ActivityLog.class,
                Document.class
        );

        Document result = results.getUniqueMappedResult();

        if (result == null) {
            return ActivityTotals.createEmptyTotals(targetDate);
        }

        return ActivityTotals.builder()
                .date(targetDate)
                .totalLeftClicks(getLongValue(result, "totalLeftClicks"))
                .totalRightClicks(getLongValue(result, "totalRightClicks"))
                .totalMiddleClicks(getLongValue(result, "totalMiddleClicks"))
                .totalKeyPresses(getLongValue(result, "totalKeyPresses"))
                .totalMouseMovement(getLongValue(result, "totalMouseMovement"))
                .build();
    }

    private long getLongValue(Document document, String key) {
        Number value = (Number) document.get(key);
        return value != null ? value.longValue() : 0L;
    }

    public List<ActivityLog> getAllLogs() {
        return mongoTemplate.findAll(ActivityLog.class);
    }
}


