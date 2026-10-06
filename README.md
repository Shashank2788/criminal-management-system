# Criminal Data Management System

Stack: Java 17, Spring Boot 3 (Web, Data JPA, Validation), MySQL, REST API, HTML, CSS, JavaScript.

## Run
1. Install JDK 17+, Maven and MySQL. Start MySQL.
2. Edit `src/main/resources/application.properties` (username/password).
   The `criminal_db` database and its tables are created automatically.
3. `mvn spring-boot:run`
4. Open http://localhost:8080

## Login and roles
| Username | Password | Role | Can do |
|---|---|---|---|
| admin | admin123 | ADMIN | everything, including delete |
| officer | officer123 | OFFICER | view, add, edit (no delete) |

The two accounts are created on first start with BCrypt-hashed passwords. Change them (or edit the
`app_user` table) before using the system for anything real.

## Structure
`controller` (REST endpoints) -> `service` (business logic) -> `repository` (Spring Data JPA) -> MySQL.
`config/SecurityConfig` holds Spring Security (form login, roles, CSRF). The UI is in `src/main/resources/static`.

## REST API
`GET /api/stats` (dashboard numbers) and `GET /api/me` (current user) are also available. All `/api` calls need a login;
`DELETE` needs the ADMIN role.

| Method | URL | Purpose |
|---|---|---|
| GET | /api/criminals?q= | list / search criminals |
| GET | /api/criminals/{id} | one criminal |
| GET | /api/criminals/{id}/cases | cases of a criminal |
| POST | /api/criminals | create |
| PUT | /api/criminals/{id} | update |
| DELETE | /api/criminals/{id} | delete (and their cases) |
| GET/POST/PUT/DELETE | /api/cases[/{id}] | same operations for cases (`?q=` searches case no. / crime type) |

Case JSON example:
`{"caseNumber":"CR-2026-001","crimeType":"Robbery","criminal":{"id":1},"dateOfCrime":"2026-08-14","status":"OPEN"}`
