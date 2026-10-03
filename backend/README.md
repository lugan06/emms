# EEMS Backend

Spring Boot 3.x backend skeleton for the event management system.

## Requirements

- JDK 17+
- Maven 3.9+
- MySQL 8.x
- Local database: `event_system`

## Local configuration

The default profile is `local` and connects to `localhost:3306/event_system`.

Override the local database settings with environment variables when needed:

```text
DB_LOCAL_URL
DB_LOCAL_USERNAME
DB_LOCAL_PASSWORD
JWT_SECRET
```

## Run

```text
mvn spring-boot:run
```

The project currently contains infrastructure and common classes only. Business controllers, services, mappers and entities will be added by module in later steps.
