
# 🚀 **User Management Service**

*A fully containerized, production-style microservice responsible for user identity, authentication, authorization, and password recovery.*

This service is built using **Spring Boot**, **Spring Security (JWT)**, **MySQL**, and integrates with an external [**Notification Service**](https://github.com/sanah-saleem/NotificationService/blob/main/README.md) (via Docker) for sending OTPs, password reset emails, and other communication workflows.

It is designed following clean modular boundaries:

* **User Management Service** → owns user accounts, credentials, JWT auth
* **Notification Service** → owns email/OTP delivery (Kafka + Redis + Mailhog)

Both services run together through **Docker Compose** for an instant plug-and-play development environment.

---

# 📦 **Docker Images (Public)**

Both microservices are published on Docker Hub:

| Service                     | Image                                   |
| --------------------------- | --------------------------------------- |
| **User Management Service** | `sanah22/user-management-service:0.1.0` |
| **Notification Service**    | `sanah22/notification-service:0.1.0`    |

These are automatically pulled by the provided `docker-compose.yml`.

---

# 🧰 **Tech Stack**

### **Backend**

* Java 17
* Spring Boot 3.x
* Spring Web
* Spring Security (JWT)
* Spring Data JPA (Hibernate)
* jakarta.validation

### **Database**

* MySQL 8
* Hibernate ORM
* Auto schema generation (development)

### **Authentication**

* JWT (HS256, stateless)
* BCrypt password hashing

### **Infrastructure**

* Docker & Docker Compose
* Kafka (event-based notifications)
* Redis (OTP caching on Notification Service side)
* Mailhog (email testing)

### **Documentation & Tools**

* Swagger / OpenAPI
* Postman Collection included in repo
* `.env` configuration

---

# ⚙️ **Architecture Overview**

```
                        +-----------------------------+
                        |  User Management Service    |
                        |  (Spring Boot + JWT + JPA)  |
                        +-----------------------------+
                                |            ^
                                | REST       | Kafka (future)
                                v            |
                   +------------------------------+
                   |    Notification Service      |
                   | (Email + OTP + Kafka + Redis)|
                   +------------------------------+
                                 |
                                 v
                        External Providers
                      (Mailhog / SMTP / SMS)
```

* This service performs **authentication**, **authorization**, **admin user management**, and **password reset**.
* Notification Service handles **OTP**, **email templates**, and **message delivery**, running as a separate microservice.

---

# 🚀 **How to Run the Service (Recommended: Docker Compose)**

This repository includes a full `docker-compose.yml` that starts:

* User Management Service
* Notification Service
* MySQL
* Kafka
* Redis
* Mailhog

Everything runs together automatically.

---

## **1️⃣ Clone the repository**

```bash
git clone https://github.com/sanah-saleem/user-management.git
cd user-management-service
```

---

## **2️⃣ Create your `.env` file**

A template is provided:

```bash
cp .env.example .env
```

`.env.example` contains:

```env
# App
APP_PORT=8080

# MySQL
MYSQL_DATABASE=user_management
MYSQL_ROOT_PASSWORD=root
MYSQL_PORT=3307

# JWT
APP_JWT_SECRET=change-this-secret-before-running
APP_JWT_EXPIRY_MINUTES=60
```

Modify values if needed.

---

## **3️⃣ Start all services**

```bash
docker compose up
```

Or without logs:

```bash
docker compose up -d
```

Docker will automatically pull:

* `sanah22/user-management-service:0.1.0`
* `sanah22/notification-service:0.1.0`
* Kafka, Redis, MySQL, Mailhog

---

## **4️⃣ Access the services**

| Component                | URL                                                                                        |
| ------------------------ | ------------------------------------------------------------------------------------------ |
| **User Management API**  | [http://localhost:8080](http://localhost:8080)                                             |
| **Swagger UI**           | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| **Health Check**         | [http://localhost:8080/api/health](http://localhost:8080/api/health)                       |
| **Notification Service** | [http://localhost:8082](http://localhost:8082)                                             |
| **MailHog UI (Emails)**  | [http://localhost:8025](http://localhost:8025)                                             |

Try triggering "forgot password" → you’ll see the email appear in MailHog.

---

# 📁 **Postman Collection**

A Postman collection is included in the root folder:

```
UserManagement.postman_collection.json
```

It contains ready-made requests for:

* Registration
* Login
* Profile
* Change password
* Forgot password
* Reset password
* Admin API endpoints

Import this file into Postman to test the system easily.

---

# 🔐 **Authentication & Security**

### 🔸 Passwords

Stored using **BCrypt** with 10–12 rounds.

### 🔸 JWT Tokens

Stateless authentication:
`Authorization: Bearer <token>`

JWT contains:

* subject (email)
* token issued timestamp
* expiry timestamp

### 🔸 User Status Validation

Every authenticated request checks:

* ACTIVE → allowed
* INACTIVE → blocked
* DELETED → blocked

---

# 🧩 **Integration With Notification Service**

This service connects to **Notification Service** for:

✔ OTP sending
✔ Email delivery (password reset, welcome emails)
✔ Messaging workflows (future: Kafka-based events)

Configured via:

```properties
notification.service.base-url=http://notification-service:8082
```

The Notification Service handles:

* OTP generation & verification
* Email templating
* Kafka-based notification events
* Redis caching of OTPs
* Mailhog for email debugging

Both services run together through Docker Compose.

---

# 📚 **API Summary**

### **Auth APIs**

* `POST /api/auth/register`
* `POST /api/auth/login`
* `POST /api/auth/forgot-password`
* `POST /api/auth/reset-password`

### **User APIs**

* `GET /api/users/me`
* `PUT /api/users/me`
* `POST /api/users/change-password`

### **Admin APIs**

All under `/api/admin/users/**`

* List users (with filtering, paging, soft delete options)
* Update user
* Deactivate / Reactivate
* Soft delete / Restore

Full API documentation available at Swagger.

---

# 🗄️ **Database Schema (Core Tables)**

### `users`

* id
* email
* password_hash
* full_name
* role (USER, ADMIN)
* status (ACTIVE/INACTIVE)
* phone
* deleted
* deleted_at
* created_at
* updated_at

### `password_reset_tokens`

* id
* token
* user_id
* created_at
* expires_at
* used

---

# 🔮 **Future Enhancements**

* Email verification flow
* OTP-based login or 2FA
* Kafka event publishing (`user.registered`, etc.)
* Login attempt rate limiting
* Device/IP-based login tracking
* Role/permission (RBAC/ABAC) system
* Flyway/Liquibase DB migrations
* Audit logs
* Multi-tenant support

---

# 🧑‍💻 **Development (Local)**

If you want to run *without Docker*:

1. Start MySQL manually.
2. Create DB:

   ```sql
   CREATE DATABASE user_management CHARACTER SET utf8mb4;
   ```
3. Update `application.properties` accordingly.
4. Run:

   ```bash
   mvn spring-boot:run
   ```

---

# 🎉 **Conclusion**

This User Management Service is designed to be:

* **Production-ready**
* **Containerized**
* **Extensible**
* **Secure**
* **Cleanly separated** from Notification concerns

Just clone → copy `.env` → `docker compose up` → done.


Just tell me!
