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

The V2 authentication flow resolves the login site from the request Host. An
`EDITOR` must have a current `admin_user_site` grant; `SUPER_ADMIN` can log in to
any enabled site. The request body contains only username and password. Login
and public endpoints reject `X-Site-Id`.

```text
POST /api/admin/login
Content-Type: application/json

{"username":"admin","password":"<password>"}
```

The response includes the JWT and also returns it in the `Authorization: Bearer <token>` response header. Send that header on subsequent `/api/admin/**` requests.

JWTs include `userId`, `loginSiteId` and `authVersion`. Every protected request
reloads the account from the database and checks its enabled/deleted state,
current role and authentication version. Tokens issued before this change must
be replaced by logging in again. An EDITOR token is limited to its login site,
and its current grant is checked on every request; visiting another granted
site requires login at that site's domain.

Password changes atomically replace the BCrypt hash and increment `auth_version`;
all prior tokens then return HTTP 401. A disabled/deleted account is rejected
immediately. The batch-4 account-management service also increments the version
for status transitions, password resets and site reassignment, so re-enabling an
account cannot restore its prior tokens.

### Filter order and site switching

The servlet `DomainSiteFilter` runs before Spring Security, resolves the original
Host, stores the host site and owns ThreadLocal cleanup in `finally`. Inside the
security chain, `JwtAuthenticationFilter` runs first, then `SiteContextFilter`,
then path/method authorization and controllers. JWT and site filters have their
automatic servlet registrations disabled to prevent duplicate execution.

Only SUPER_ADMIN may override the effective site using a single `X-Site-Id`
header containing a positive Long ID. The target must exist; disabled sites
are available for backend maintenance, while public requests and new logins
remain blocked. Tenant SQL filtering stays enabled after an override.

The header is accepted only on `/api/admin/me`, `/api/admin/site` and the
exhibitions/categories/contents/files/guestbooks resource trees. Company data,
operation logs, password changes, site/user management and undeclared endpoints
reject it with HTTP 400. EDITOR requests carrying the header return HTTP 403,
even when its value equals the host site or is malformed. Missing/invalid JWTs
return HTTP 401 before any override is evaluated. Malformed/repeated override
headers return HTTP 400, and nonexistent targets return HTTP 404.

OPTIONS bypasses the custom domain/identity/site filters. OpenAPI and Swagger
remain available without a site context. `/v3/api-docs` documents the optional
header only for the supported resource paths. CORS allows the header; configure
`CORS_ALLOWED_ORIGIN_PATTERNS` with comma-separated origins per environment
(defaults are localhost and 127.0.0.1 development origins). A shared frontend/API
origin is recommended so domain-based site selection reaches the API Host.

Authentication failures and site authorization refusals use independent audit
transactions. Only whitelisted context metadata is logged, never raw JWTs,
passwords or override-header values. Production unknown hosts do not fall back
to the default site; the existing local/test fallback remains available.

## V2 batch 3: site management

Only SUPER_ADMIN can use the global site-management endpoints. They do not
accept X-Site-Id. Existing site-specific configuration remains at /api/admin/site.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | /api/admin/sites | Paginate with page/pageSize, keyword (name/code/domain), status |
| POST | /api/admin/sites | Create a disabled site, domain bindings and its default configuration |
| PUT | /api/admin/sites/{id} | Update name and replace the complete domain list; code is immutable |
| PUT | /api/admin/sites/{id}/status | Set status to ENABLED or DISABLED |

Create/update body:

```json
{
  "name": "新站点",
  "code": "new-site",
  "domains": [
    { "domain": "expo.example.com", "primary": true },
    { "domain": "www.expo.example.com", "primary": false }
  ]
}
```

Each site has 1–50 domains and exactly one primary domain. Hostnames are trimmed
and lowercased, globally unique, ASCII (use punycode for international domains),
and contain no protocol, port, path, wildcard or trailing dot. Database unique
constraints remain the final guard against concurrent duplicate requests.
Codes use 1–50 lowercase letters/digits/underscores/hyphens and start with a
letter or digit. Status changes use a body such as {"status":"ENABLED"}.

Creation, domain insertion, unique site_config initialization (config_code =
default, site_title = name, domain = primary hostname) and success auditing share
one transaction. Audit failure rolls everything back. Configuration creation
uses the explicit new site ID without replacing the request's SiteContext.
Editing site metadata does not overwrite independently maintained site_config
branding. No new production migration is required beyond batches 1 and 2.

To try the UI locally, log in as SUPER_ADMIN, open 站点管理 (/admin/sites), click
新建站点, enter a unique code and hostname, and save. The new entry starts
停用; click 启用 after checking its domains. Editing locks the code and lets you
add/remove aliases or select a different primary domain. DNS/hosts and the
reverse proxy must route the hostname to the application before domain access
can work. No DNS or proxy configuration is changed by creating a database row.

## V2 batch 4: administrators and grants

Open 管理员管理 (/admin/users) as SUPER_ADMIN. The page lists ordinary
administrators and provides create, nickname edit, site assignment, status and
password-reset actions. The sidebar and route guard restrict access to super
administrators; backend authorization independently enforces that restriction.

| Method | Path | Purpose / body |
| --- | --- | --- |
| GET | /api/admin/users | Paginate with keyword (username/nickname), status, siteId, page, pageSize |
| POST | /api/admin/users | Create with username, password, nickname, siteIds |
| PUT | /api/admin/users/{id} | Update nickname only |
| PUT | /api/admin/users/{id}/sites | Replace the complete siteIds array |
| PUT | /api/admin/users/{id}/status | Set status to ENABLED or DISABLED |
| PUT | /api/admin/users/{id}/password | Reset with password |
| GET | /api/admin/sites/options | All sites as id/name/code/status, including disabled sites |

Creation always assigns EDITOR, ENABLED and authentication version 0; request
fields cannot override the role. All mutation targets must be non-deleted EDITOR
accounts; SUPER_ADMIN, unknown roles, deleted and missing accounts return 404.
Usernames are immutable, unique and 1–50 ASCII letters/digits/underscores/dots/@/
hyphens, starting with a letter or digit. Nicknames contain 1–100 characters.
Passwords require at least 8 characters and at most 72 UTF-8 bytes (the BCrypt
limit); passwords are never trimmed, returned, or included in audit metadata.

siteIds is required, contains up to 1000 distinct positive IDs, and every site
must exist. Disabled sites may be granted for later use. An empty array revokes
all grants; the account then cannot log in to any site. Site grants do not
override a site's disabled status.

Each mutation and its success audit share a transaction. Account row locks
serialize changes; updates additionally require EDITOR and the expected
authentication version. Grant replacement deletes and inserts bindings in that
same transaction and increments auth_version. Password reset and every effective
status transition increment it as well; all old tokens return 401. An idempotent
status request preserves the version. Nickname editing preserves active tokens.
No new database migration is needed.

For local permission verification:

1. Create/enable two sites A and B with different domains routed to the API.
2. Create an administrator with only A selected. Login at A succeeds; login at B
   returns 403. The account cannot call /api/admin/users or /api/admin/sites/options.
3. Reassign the account to B. Its old A token returns 401; a new login at A
   returns 403, while login at B succeeds.
4. Disable the account: its B token returns 401. Re-enable: that old token still
   returns 401; a new login succeeds.
5. Reset its password: the current token returns 401, the old password fails,
   and the reset password succeeds. Clear all grants to block every site login.

Existing domain security is unchanged: these global management endpoints reject
X-Site-Id, and an EDITOR token remains bound to the domain of its login site.
The Vite development proxy preserves the browser's Host so local A/B domains
reach the correct SiteContext; direct API tests can send Host explicitly.
For example, bind a.localhost and b.localhost to sites A/B, resolve them to
127.0.0.1 (add hosts entries if needed), and open each domain at the frontend
development port. These localhost subdomains are accepted by Vite's default
host check. Binding a domain in the database does not configure DNS or the reverse
proxy. Production proxies must also preserve the original Host.

## Run

```text
mvn spring-boot:run
```

## Test

Unit tests use mocks; integration tests use the independent H2 database under the
`test` profile. They do not connect to the development or production database.

```text
mvn test
```

The authentication integration suite exercises real JWTs, BCrypt passwords,
account/grant changes, site overrides, tenant isolation and OpenAPI output.
