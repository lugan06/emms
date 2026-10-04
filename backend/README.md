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

## Seed initial administrator

The seed runner is disabled by default. To create the first administrator on application startup, set these environment variables once:

```text
EEMS_SEED_ADMIN_ENABLED=true
EEMS_SEED_ADMIN_USERNAME=admin
EEMS_SEED_ADMIN_PASSWORD=<strong-password>
EEMS_SEED_ADMIN_NICKNAME=系统管理员
EEMS_SEED_ADMIN_ROLE_CODE=SUPER_ADMIN
```

If the username already exists, the seed runner skips it and never overwrites the existing password.

## Login

```text
POST /api/admin/login
Content-Type: application/json

{"username":"admin","password":"<password>"}
```

The response includes the JWT and also returns it in the `Authorization: Bearer <token>` response header. Send that header on subsequent `/api/admin/**` requests.

## Run

```text
mvn spring-boot:run
```

## Test

The login, BCrypt verification, JWT validation and administrator seed samples use mocks and do not require a database connection.

```text
mvn test
```

The project currently contains infrastructure and common classes only. Business controllers, services, mappers and entities will be added by module in later steps.
