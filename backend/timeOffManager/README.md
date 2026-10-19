# Time Off Manager — Backend

Spring Boot 3 REST API service powering the Time Off Manager platform.

---

## Tech Stack & Specifications

- **Language:** Java 25
- **Framework:** Spring Boot 3.5.6
- **Security:** Spring Security (Stateless JWT authentication, BCrypt password hashing, method-level `@PreAuthorize` authorization)
- **Email Service:** Spring Mail integrated with Mailpit (local) and AWS SES (production)
- **Monitoring & Health:** Spring Boot Actuator (`/actuator/health`)
- **Persistence:** Spring Data JPA, Hibernate 6.6
- **Database Migrations:** Flyway 11 (`flyway-core`, `flyway-database-postgresql`)
- **Databases:** PostgreSQL 16 (production profile), H2 In-Memory (local dev/tests)
- **Object Mapping:** MapStruct 1.6.3
- **Validation:** `spring-boot-starter-validation` (Jakarta Validation)
- **PDF Generation:** LibrePDF OpenPDF 2.0.3
- **Documentation:** Springdoc OpenAPI 2.8.13 (Swagger UI, active in dev profile)
- **Error Handling:** RFC 7807 `ProblemDetail` via `@RestControllerAdvice`

---

## Prerequisites

- **Java JDK 25** installed and set as `JAVA_HOME`
- **Apache Maven 3.9+** (or use bundled Maven wrapper)

---

## Getting Started

### 1. Compile and Validate Build

From this directory (`backend/timeOffManager`):

```bash
mvn clean test-compile
```

### 2. Run the Application (Local Profile)

```bash
mvn spring-boot:run
```

The application starts on port `8080` with the `local` profile (in-memory H2, seed data, and Swagger enabled):

- **API Base URL:** `http://localhost:8080/api`
- **Health Check:** `http://localhost:8080/actuator/health`
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: empty)

---

## Core REST Endpoints

### 1. Authentication (`/api/auth`)
- `POST /api/auth/login`: Authenticate with email and password; returns JWT token and user info.
- `POST /api/auth/forgot-password`: Request a secure password reset link sent via email.
- `POST /api/auth/reset-password`: Set a new password using a one-time reset token.
- `GET /api/auth/generate-password`: Generate a strong temporary password.

### 2. Requests (`/api/request`)
- `GET /api/request/{id}`: Get request details by ID.
- `POST /api/request`: Submit a new leave request (enforces start <= end, Mon-Fri working days, non-overlapping requests, and balance sufficiency).
- `PUT /api/request/{id}`: Edit pending/edited request (revalidates dates and balance).
- `PATCH /api/request/{id}/status?status={STATUS}`: Transition request status according to business state machine.
- `DELETE /api/request/{id}`: Delete request.
- `GET /api/request/pending/{managerId}?includeUnassigned={boolean}`: Get pending/edited requests for direct reports.
- `GET /api/request/employee/{employeeId}`: Get paginated requests for an employee.

### 3. Balances (`/api/balance`)
- `GET /api/balance/{id}`: Retrieve balance allowance.
- `POST /api/balance`: Create an allowance for an employee (Admin only).
- `PUT /api/balance/{id}`: In-place update of allowance (`name`, `daysLeft`, `maxDays`).
- `DELETE /api/balance/{id}`: Safely unlink and delete an allowance.
- `GET /api/balance/employee/{employeeId}`: Get allowances for an employee.

### 4. Projects (`/api/projects`)
- `GET /api/projects`: List projects.
- `GET /api/projects/{id}`: Get project details.
- `POST /api/projects`: Create project.
- `PUT /api/projects/{id}`: Update project details and `minEmp` staffing threshold.
- `DELETE /api/projects/{id}`: Delete project.
- `GET /api/projects/{id}/members`: List all members assigned to project.
- `POST /api/projects/{id}/members/{employeeId}`: Add employee to project.
- `DELETE /api/projects/{id}/members/{employeeId}`: Remove employee from project.

### 5. Users (`/api/customer`)
- `GET /api/customer`: List employees/managers/admins.
- `GET /api/customer/{id}`: Get user by ID.
- `POST /api/customer`: Create user (sends welcome email with initial setup credentials).
- `PUT /api/customer/{id}`: Update user, roles, and reporting lines.
- `DELETE /api/customer/{id}`: Soft-delete user (`active=false`) preserving leave history.
- `POST /api/customer/{id}/reset-password`: Admin triggers password reset email.

### 6. Team Absence Calendar (`/api/calendar`)
- `GET /api/calendar?from={date}&to={date}&projectId={id}`: Get team absences and coverage for month grid.

### 7. Reports (`/api/report`)
- `GET /api/report/project/{projectId}/pdf?timePeriod={WEEKLY|MONTHLY|YEARLY}`: Generate PDF availability report.

---

## Business Logic & State Machine

1. **Working Days Calculation:** Evaluated Monday through Friday, excluding weekends.
2. **Atomic Balance Deductions:** Approvals atomically decrement days from balance; cancellations restore days.
3. **Optimistic Locking:** `balance.version` prevents concurrent lost-update anomalies during simultaneous approvals.
4. **Soft-Delete Integrity:** Deactivated users retain full historical requests and audit trails.

---

## Running Automated Tests

```bash
mvn test
```
