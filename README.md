# Activity Tracker

A comprehensive user activity tracking application built in Java.
The Activity Tracker captures and analyzes keyboard and mouse activity, window focus,
and application usage across multiple monitors. It provides insightful analytics to help users understand their time
usage better.

---

## Features

### 🎯 Real-Time Activity Monitoring

- **Keyboard and Mouse Tracking**: Measures key presses and mouse actions, including movement and clicks (left, right,
  and middle).
- **Multi-Monitor Support**: Tracks activity across multiple monitors, including focused windows.
- **Idle State Detection**: Identifies idle periods based on user inactivity thresholds.

### 📊 Analytics and Reporting

- **Activity Logs**: Captures detailed activity metrics with timestamps.
- **Usage Statistics**: Aggregates application and window usage statistics by time spent and activity patterns.
- **Idle and Active Time Analysis**: Differentiates between idle and productive times.

### 🛠 REST API

- **Activity Logs**: RESTful endpoints to query logs, activity totals, and date ranges.
- **Application Usage Reports**: Retrieve breakdowns of time spent on different applications and categories.

### 🛡 Built with Robust Systems Integration

- Uses **JNativeHook** and **JNA** for native Windows OS event listening.
- Stores activity data in **MongoDB** for long-term analytics, with Docker Compose support for easy setup.

---

## Installation

### 1. Clone the Repository

Download the project code using Git:
```bash
git clone https://github.com/ayagmar/activity-tracker.git
cd activity-tracker
```

### 2. Prerequisites

Ensure you have the following installed:

- [Java 21 (JDK)](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)

### 3. MongoDB Setup

Start a MongoDB instance using Docker Compose:
```bash
docker-compose up -d
```

### 4. Build the Application

Compile and package the project with Maven:
```bash
mvn clean install
```

### 5. Run the Application

Start the Activity Tracker:
```bash
mvn spring-boot:run
```

The application will by default run on [http://localhost:8090](http://localhost:8090).

---

## Usage

### API Usage Examples

#### Retrieve All Activity Logs

```bash
GET /api/v1/activity-logs
```

#### Retrieve Logs by Date Range

```bash
GET /api/v1/activity-logs/range?start=2023-10-01T00:00:00&end=2023-10-01T23:59:59
```

#### Retrieve Totals for a Specific Date

```bash
GET /api/v1/activity-logs/totals?date=2023-10-01
```

#### Retrieve Application Usage Report

```bash
GET /api/v1/application-usage/report?start=2023-10-01T00:00:00&end=2023-10-01T23:59:59
```

Refer to the source code for additional endpoints and parameters.

---

## Configuration

Key settings can be updated in `src/main/resources/application.properties`.

### Application Behavior
```properties
activity.idle.threshold-minutes=5    # Threshold for idle state detection (in minutes)
activity.logging.interval=1          # Interval for logging user activity (in minutes)
activity.metrics.dpi=91.79           # DPI of screen (affects mouse movement measurement)
```

### MongoDB
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/activityTrackerDB
```

### Port and Server Configuration

```properties
server.port=8090 # Change this if you need the server to use a different port
```

---

## Testing Instructions

Additional integration testing for REST endpoints can be performed using tools like [Postman](https://www.postman.com/)
or `curl` commands. For example:

```bash
curl -X GET "http://localhost:8090/api/v1/activity-logs"
```

Make sure the required MongoDB instance is running when testing the REST API.

---

## Architecture

### Key Components

- **Listeners:** Capture system events, such as keyboard and mouse interactions, using native hooks.
- **Idle Detection:** Detects user inactivity and manages idle periods.
- **Process Tracker:** Tracks running processes and determines active applications.
- **Metrics Collection:** Aggregates user activity data into meaningful logs.
- **REST API:** Provides accessible endpoints for activity and application usage analysis.

### Data Flow

1. **Event Listeners:** Captures system events.
2. **Activity Logging:** Stores processed user activity metrics into MongoDB.
3. **Data Exposure:** Provides analytics and reports via REST API.

### Core Libraries

- **Spring Framework**: Dependency injection, schedulers, and REST controllers.
- **MongoDB**: Efficient storage and querying of activity data.
- **Native System Hooks**: Monitoring keyboard, mouse, and Windows processes.

For more implementation details, refer to the `src` folder, which follows clean separation of concerns.

---
