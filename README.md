# Time Off Manager

A full-stack enterprise web application for managing employee time-off requests, team projects, leave balances, and availability reporting with role-based access control (Employee, Manager, Admin).

---

## Architecture & Tech Stack

- **Backend:**
  - Java 25
  - Spring Boot 3.5.6 (Spring Web, Spring Data JPA, Spring Validation, Spring Mail, Spring Boot Actuator)
  - Spring Security (Stateless JWT authentication, BCrypt password hashing, method-level security)
  - Flyway 11 Database Migrations (`flyway-core`, `flyway-database-postgresql`)
  - MapStruct 1.6.3 (Entity-DTO mapping)
  - LibrePDF OpenPDF 2.0.3 (PDF report generation)
  - Springdoc OpenAPI 2.8.13 (Swagger UI documentation, dev only)
  - Databases: H2 In-Memory (development/tests) and PostgreSQL 16 (production)
- **Frontend:**
  - Angular 19.2 (Standalone Components, Signals, Reactive Forms, SCSS)
  - TypeScript 5.7+
  - Development Proxy (`proxy.conf.json`) to forward `/api` requests to backend port 8080
- **Deployment & Cloud Infrastructure:**
  - Multi-stage Docker builds (Java 25 JRE runtime + Nginx serving Angular SPA)
  - Docker Compose orchestration (`backend`, `frontend`, `postgres:16`, `mailpit`)
  - Single-origin Nginx reverse proxy forwarding `/api` to backend (no CORS overhead)
  - Cloud Architecture (AWS Option B): Single EC2 host (`t4g.small`), AWS Managed RDS PostgreSQL (`db.t4g.micro`), and AWS SES for live transactional emails

---

## Repository Structure

```text
wad_time_off_manager/
├── docker-compose.yml             # Local parity stack (Postgres, Mailpit, Backend, Frontend)
├── .env.example                   # Template for AWS RDS, SES, JWT, and Admin credentials
├── backend/
│   └── timeOffManager/            # Spring Boot application
│       ├── Dockerfile             # Multi-stage Java 25 Docker build
│       ├── pom.xml
│       └── src/
│           ├── main/
│           │   ├── java/com/project/timeOffManager/
│           │   │   ├── config/      # SecurityConfig (JWT), WebConfig, AdminBootstrapInitializer
│           │   │   ├── controller/  # REST controllers (Auth, Request, Balance, Projects, Customer, Report, Calendar)
│           │   │   ├── dto/         # Command and Response DTOs
│           │   │   ├── entity/      # JPA entities (User, Request, Project, Balance, PasswordResetToken)
│           │   │   ├── enums/       # Status, TimePeriod, Role
│           │   │   ├── exception/   # RFC 7807 ProblemDetail GlobalExceptionHandler
│           │   │   ├── mapper/      # MapStruct DTO mappers
│           │   │   ├── repository/  # Spring Data JPA repositories
│           │   │   ├── security/    # JwtService, JwtAuthenticationFilter, AuthorizationService
│           │   │   └── service/     # Business logic, state machine, EmailService
│           │   └── resources/
│           │       ├── application.yml         # Base configuration
│           │       ├── application-local.yml   # Local profile (H2, Swagger, dev headers)
│           │       ├── application-prod.yml    # Production profile (PostgreSQL, Actuator, SES)
│           │       └── db/
│           │           ├── migration/          # Versioned Flyway DDL (V1, V3, V5)
│           │           └── seed/               # Local dev seed data (V2, V4)
│           └── test/
└── frontend/                      # Angular 19 application
    ├── Dockerfile                 # Multi-stage Angular build + Nginx Alpine
    ├── nginx.conf                 # Nginx reverse proxy (/api -> backend) & SPA router
    ├── angular.json
    ├── package.json
    ├── proxy.conf.json            # Local dev proxy (/api -> http://localhost:8080)
    └── src/
        ├── environments/          # Environment configuration (dev / prod)
        ├── app/
        │   ├── common/            # Header, Sidebar
        │   ├── interceptors/      # AuthInterceptor (JWT Bearer token attachment)
        │   ├── pages/             # Login, Reset Password, Overview, New Request, History, Projects, Approvals, Administration, Calendar
        │   └── services/          # ApiService, SessionService, ToastService
        └── main.ts
```

---

## Quick Start with Docker (Recommended - Local Parity)

The entire production stack (PostgreSQL 16, Mailpit mock email server, Java 25 backend, and Angular on Nginx) can be launched in one command:

```powershell
cd wad_time_off_manager
docker compose up -d --build
```

- **Web Application:** [http://localhost](http://localhost)
  - **Default Admin Login:** `alice.smith@timeoff.com` / `password123`
- **Mailpit Web Inbox:** [http://localhost:8025](http://localhost:8025) (Inspect password reset and welcome emails)
- **Actuator Health Check:** [http://localhost/actuator/health](http://localhost/actuator/health)

To stop the containers:
```powershell
docker compose down
```

---

## Manual Development Quick Start

### 1. Run the Backend

```bash
cd backend/timeOffManager
mvn clean test-compile
mvn spring-boot:run
```

- **Backend API:** `http://localhost:8080/api`
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: empty)

### 2. Run the Frontend

```bash
cd frontend
npm install
npm start
```

- **Frontend URL:** [http://localhost:4200](http://localhost:4200)
- Requests to `/api/*` are forwarded by the Angular CLI dev server to `http://localhost:8080/api/*`.

---

## Production Cloud Deployment (AWS Option B)

1. Provision an **AWS RDS PostgreSQL** instance (`db.t4g.micro`, PostgreSQL 16) and configure its security group to accept port 5432 from your EC2 instance.
2. In **AWS SES**, verify your sender identity and generate SMTP credentials.
3. Launch an **AWS EC2** instance (`t4g.small` Ubuntu or Amazon Linux) with Docker and Docker Compose installed.
4. Copy the project to the EC2 instance and configure environment variables:
   ```bash
   cp .env.example .env
   nano .env
   ```
   Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `MAIL_HOST`, `MAIL_USERNAME`, and `MAIL_PASSWORD`.
5. Launch the containers:
   ```bash
   docker compose up -d --build
   ```

---

## Running Automated Tests

```bash
# Backend Tests (Spring Boot Test, Security, State Machine)
cd backend/timeOffManager
mvn test

# Frontend Unit Tests
cd frontend
npm test
```