# Activity Tracker

## Overview

Activity Tracker is a sophisticated Java application that monitors and analyzes user activity on Windows systems. It
tracks keyboard and mouse inputs, window focus, and application usage across multiple monitors to provide detailed
activity analytics.

## Features

- Real-time activity monitoring
    - Keyboard and mouse input tracking
    - Multi-monitor support
    - Application usage statistics
    - Idle state detection
- Comprehensive analytics
    - Activity totals by date
    - Application usage reports
    - Time-based activity analysis
- REST API for data access
    - Activity logs retrieval
    - Usage statistics
    - Data export capabilities

## Technical Stack

- Java 21
- Spring Boot 3.4.1
- MongoDB
- JNA (Java Native Access)
- JNativeHook
- Docker for database containerization

## Prerequisites

- JDK 21
- Docker and Docker Compose
- Windows Operating System
- Maven

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/activity-tracker.git
cd activity-tracker
```

### 2. Start MongoDB

```bash
docker-compose up -d
```

### 3. Build the Application

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on port 8090 by default.

## Configuration

### Application Properties

Key configuration properties in `application.properties`:

```properties
activity.idle.threshold-minutes=5    # Idle detection threshold
activity.logging.interval=1          # Activity logging interval in minutes
```

### MongoDB Configuration

The application uses MongoDB for data storage. Default connection string:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/activityTrackerDB
```

## API Documentation

### Activity Logs

- `GET /api/v1/activity-logs` - Retrieve all activity logs
- `GET /api/v1/activity-logs/totals` - Get activity totals for a specific date
- `GET /api/v1/activity-logs/range` - Get logs within a date range

### Application Usage

- `GET /api/v1/application-usage/report` - Get application usage statistics

## Architecture

### Key Components

1. **Listeners**
    - `MetricsCollector`: Captures input events
    - `IdleStateManager`: Manages user idle state
    - `MousePositionTracker`: Tracks mouse movement

2. **Process Management**
    - `ProcessTracker`: Monitors running processes
    - `WindowManager`: Manages window focus and positioning

3. **Services**
    - `ActivityLogService`: Handles activity logging
    - `ActivityUsageService`: Processes usage statistics

### Data Flow

1. Native system events → Listeners
2. Listeners → Metrics collection
3. Metrics → Activity logs
4. Activity logs → MongoDB
5. MongoDB → REST API

## Security Considerations

- The application requires system-level access for activity monitoring
- API endpoints should be secured in production
- Data retention policies should be implemented
- User privacy considerations should be addressed


