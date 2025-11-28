# User Management Service

A production-style **User Management microservice** built with **Spring Boot, Spring Security, JPA, and MySQL**, designed to act as the central source of truth for users in a microservice ecosystem.

It exposes REST APIs for:

- User registration and login (JWT-based)
- Profile management and password changes
- Admin management of users (roles, status, soft delete)
- Password reset with secure tokens
- Health checks and OpenAPI/Swagger documentation

It is designed to integrate with a separate **Notification Service** for OTPs, welcome emails, and generic mail workflows.

---

## 🔧 Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot
  - Spring Web
  - Spring Data JPA (Hibernate)
  - Spring Security (JWT-based auth)
  - Validation (Jakarta)
- **Database**: MySQL 8
- **Build**: Maven
- **Auth**: JWT (HS256)
- **Docs**: springdoc-openapi + Swagger UI
- **Containerization**: Docker + Docker Compose
- **Config**: `.env` + environment variables + `application.yml`

---

## 🧩 Core Features

### 1. Authentication & Authorization

- **Register** (`POST /api/auth/register`)
  - Create a new user with `email`, `fullName`, and `password`.
  - Ensures email uniqueness (excluding soft-deleted users).
  - Stores passwords with BCrypt.
  - Returns a safe `UserResponse` (no password).

- **Login** (`POST /api/auth/login`)
  - Accepts `email + password`.
  - Validates credentials and user status.
  - Issues a **JWT** (HS256) with configurable expiry.
  - Prevents login if account is **INACTIVE** or **deleted**.

- **JWT-secured APIs**
  - Clients send `Authorization: Bearer <token>`.
  - Stateless authentication via `JwtAuthFilter`.
  - Roles enforced via Spring Security:
    - `/api/admin/**` → `ROLE_ADMIN`
    - user self endpoints → authenticated user

---

### 2. User Self-Service

- **Get current user** (`GET /api/users/me`)
  - Returns the authenticated user’s profile.

- **Update profile** (`PUT /api/users/me`)
  - Update `fullName`, `phone`, and optionally `email`.
  - Email uniqueness enforced.
  - If email changes, a **new JWT** is returned so the user stays authenticated.

- **Change password** (`POST /api/users/change-password`)
  - Requires `currentPassword` + `newPassword`.
  - Verifies current password.
  - Enforces minimum length and “must be different” rule.

---

### 3. Admin User Management

All admin endpoints require `ROLE_ADMIN` and are under `/api/admin/users`.

- **Paged user listing** (`GET /api/admin/users`)
  - Supports:
    - Search by email/name (`q`)
    - Filter by `role` (`USER` / `ADMIN`)
    - Filter by `status` (`ACTIVE`, `INACTIVE`, `LOCKED`)
    - Filter by created date range (`createdFrom`, `createdTo`)
    - Optionally include deleted users (`includeDeleted=true`)
  - Uses a nice wrapper:
    ```json
    {
      "items": [ ...UserResponse ],
      "page": 0,
      "size": 10,
      "totalElements": 42,
      "totalPages": 5,
      "hasNext": true,
      "hasPrevious": false,
      "sort": "createdAt,desc"
    }
    ```

- **Update user (admin)** (`PUT /api/admin/users/{id}`)
  - Partial update of:
    - `fullName`
    - `phone`
    - `role` (`USER` / `ADMIN`)
    - `status` (`ACTIVE` / `INACTIVE` / `LOCKED`)
    - optionally `email` (with uniqueness check)

- **Deactivate / Reactivate**:
  - `POST /api/admin/users/{id}/deactivate` → sets status to `INACTIVE`
  - `POST /api/admin/users/{id}/reactivate` → sets status to `ACTIVE`
  - Deactivated users cannot log in.

- **Soft-delete / Restore**:
  - `DELETE /api/admin/users/{id}` → sets `deleted = true`, `deletedAt` and `status = INACTIVE`
  - `POST /api/admin/users/{id}/restore` → clears `deleted`/`deletedAt`
  - Soft-deleted users are excluded from normal queries and cannot log in.

---

### 4. Password Recovery

- **Forgot password** (`POST /api/auth/forgot-password`)
  - Accepts `email`.
  - If user exists and is active:
    - Generates a one-time **password reset token** (random, URL-safe, 15 min expiry).
    - Persists token in `password_reset_tokens` table.
  - For dev mode, token can be returned in response; in production, it should be sent via Notification Service (see integration section).
  - For unknown emails, returns a generic message (does not leak existence).

- **Reset password** (`POST /api/auth/reset-password`)
  - Accepts `token` + `newPassword`.
  - Validates:
    - Token exists.
    - Not expired.
    - Not already used.
    - User is ACTIVE and not deleted.
  - Updates user password, marks token as used.

---

### 5. Observability & Docs

- **Health check** (`GET /api/health`)
  - Simple JSON status with `status`, `startedAt`, `now`.

- **Swagger UI / OpenAPI**
  - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Controllers are grouped with tags for easy exploration.

---

## 📦 Integration with Notification Service (Planned)

This User Management Service is designed to integrate with a separate **Notification Service** (for example, a Spring Boot + Kafka + Redis service) that handles:

- OTP generation & verification
- Email/SMS notifications
- Templated HTML emails

### Potential integration points

1. **OTP for login / sensitive actions**
   - Add endpoints here such as:
     - `POST /api/auth/request-otp` (for email/phone)
     - `POST /api/auth/verify-otp`
   - This service:
     - Validates the user (email/phone).
     - Emits an event or calls the Notification Service to send OTP.
   - Notification Service:
     - Generates OTP, caches it (e.g. Redis), sends via email/SMS.
     - Verifies OTP on request or returns verification result via API.

2. **Welcome emails**
   - On successful registration, this service can:
     - Publish an event to a Kafka topic (e.g. `user.registered`) with user details.
     - Or call the Notification Service via REST with a "WELCOME_EMAIL" template key.
   - Notification Service renders templates and sends the email.

3. **Password reset emails**
   - When a password reset token is generated:
     - Instead of returning the raw token in the response, this service:
       - Builds a frontend URL (e.g. `https://myapp.com/reset-password?token=...`)
       - Sends it to Notification Service (email templating).
   - Notification Service handles delivering the email with appropriate template.

4. **Plain / ad-hoc mails**
   - Admin or system events (e.g. “your account has been deactivated”) can:
     - Trigger a REST call or event to Notification Service specifying:
       - Recipient email
       - Template ID (WELCOME, ACCOUNT_DEACTIVATED, PASSWORD_CHANGED, etc.)
       - Dynamic parameters (name, timestamps, etc.)

> The current codebase already has **clear boundaries**: User Management owns user data and security, while Notification Service will own mail/OTP delivery. Integration can be done via REST or Kafka without changing the core domain logic.

---

## 🗄️ Data Model (Core Tables)

### `users`

- `id` (PK)
- `email` (unique, not null)
- `full_name`
- `password_hash`
- `role` (`USER`, `ADMIN`)
- `status` (`ACTIVE`, `INACTIVE`, `LOCKED`)
- `phone` (optional)
- `deleted` (boolean)
- `deleted_at` (timestamp, nullable)
- `created_at`
- `updated_at`

### `password_reset_tokens`

- `id` (PK)
- `token` (unique)
- `user_id` (FK → users.id)
- `created_at`
- `expires_at`
- `used` (boolean)

---

## 🚀 Running the Service

### 1. Using Docker Compose (recommended)


Requirements:
- Docker
- Docker Compose

Steps:

1. Create a `.env` file in project root:

   ```env
   # App
   APP_PORT=8080

   # MySQL
   MYSQL_DATABASE=user_mgmt
   MYSQL_ROOT_PASSWORD=root
   MYSQL_PORT=3307

   # JWT
   APP_JWT_SECRET=super-secret-change-me-very-long-random-string
   APP_JWT_EXPIRY_MINUTES=60
   ```

2. Start services:

   ```bash
   docker compose up --build
   ```

3. Access:

   * API: `http://localhost:8080`
   * Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   * Health: `http://localhost:8080/api/health`

The app connects to MySQL via the `db` service inside the Docker network.

---

### 2. Running Locally (without Docker)

1. Start a local MySQL instance and create database:

   ```sql
   CREATE DATABASE user_mgmt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Configure `application.yml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/user_mgmt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
       username: root
       password: root

   app:
     jwt:
       secret: change-me-to-a-very-long-random-string
       expiry-minutes: 60
   ```

3. Run via Maven / IDE:

   ```bash
   mvn spring-boot:run
   ```

---

## 🔐 JWT & Security Notes

* JWT tokens are **stateless**:

  * No session stored on server.
  * Every request is checked via `JwtAuthFilter`.
* Token contains at least the **subject email**.
* User status is validated on login and on each authenticated request via `UserPrincipal`:

  * `ACTIVE` → allowed
  * `INACTIVE` / `deleted` → blocked
* Passwords are stored using BCrypt (via `PasswordEncoder`).

Future security enhancements (planned / optional):

* `passwordChangedAt` + token versioning to invalidate old tokens after password change.
* IP/device-aware login history.
* Rate limiting / brute-force protection on login and password reset.

---

## 🔮 Future Enhancements

Some ideas you can implement later:

1. **OTP-based flows**

   * Login with OTP (email/phone) instead of password.
   * 2FA for sensitive operations (change email, change password, delete account).

2. **Email verification**

   * On registration and email change:

     * Create a verification token.
     * Send via Notification Service.
     * Mark email as verified once the token is used.

3. **Audit logging**

   * Track who changed what and when (e.g. admin changes to roles/status).
   * Store audit records in a separate table or log index.

4. **Role & permission model**

   * Move from simple `USER` / `ADMIN` to more granular roles.
   * Optionally add permissions/claims.

5. **Flyway / Liquibase migrations**

   * Replace `ddl-auto=update` with versioned DB migrations for production.

6. **Multi-tenant support**

   * Add `tenant_id` to users.
   * Scope all queries by tenant.

7. **Metrics & Observability**

   * Add Prometheus metrics, request tracing, etc.
   * Integrate with centralized logging.

---

## 🧪 Testing

* Unit tests with JUnit + Mockito (can be added for:

  * `UserService`
  * `PasswordResetService`
  * `JwtService`
* Integration tests with:

  * Testcontainers (MySQL)
  * MockMvc for REST endpoints

