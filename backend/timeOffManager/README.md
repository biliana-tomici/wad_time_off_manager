# Time Off Manager — Backend

Spring Boot 3 REST API service powering the Time Off Manager platform.

---

## Tech Stack & Specifications

- **Language:** Java 25
- **Framework:** Spring Boot 3.5.6
- **Persistence:** Spring Data JPA, Hibernate 6.6
- **Database:** H2 Database (in-memory)
- **Object Mapping:** MapStruct 1.6.3
- **Validation:** `spring-boot-starter-validation` (Jakarta Validation)
- **PDF Generation:** LibrePDF OpenPDF 2.0.3
- **Documentation:** Springdoc OpenAPI 2.8.13 (Swagger UI)
- **Error Handling:** RFC 7807 `ProblemDetail` via `@RestControllerAdvice`

---

## Prerequisites

- **Java JDK 25** installed and set as `JAVA_HOME`
- **Apache Maven 3.9+**

---

## Getting Started

### 1. Compile and Validate Build

From this directory (`backend/timeOffManager`):

```bash
mvn clean test-compile
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application starts on port `8080`.

- **API Base URL:** `http://localhost:8080/api`
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Schema (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User: `sa`
  - Password: *(leave blank)*

---

## Core REST Endpoints

### 1. Requests (`/api/request`)
- `GET /api/request/{id}`: Get request details by ID.
- `POST /api/request`: Submit a new leave request (enforces start <= end, Mon-Fri working days, non-overlapping requests, and balance sufficiency).
- `PUT /api/request/{id}`: Edit pending/edited request (revalidates dates and balance).
- `PATCH /api/request/{id}/status?status={STATUS}`: Transition request status according to business state machine.
- `DELETE /api/request/{id}`: Delete request.
- `GET /api/request/pending/{managerId}?includeUnassigned={boolean}`: Get pending/edited requests for direct reports (or unassigned users if `includeUnassigned=true`).
- `GET /api/request/employee/{employeeId}`: Get paginated requests for an employee.

### 2. Balances (`/api/balance`)
- `GET /api/balance/{id}`: Retrieve balance allowance.
- `POST /api/balance`: Create an allowance for an employee.
- `PUT /api/balance/{id}`: In-place update of allowance (`name`, `daysLeft`, `maxDays`).
- `DELETE /api/balance/{id}`: Safely unlink and delete an allowance.
- `GET /api/balance/employee/{employeeId}`: Get allowances for an employee.

### 3. Projects (`/api/projects`)
- `GET /api/projects`: List all projects.
- `GET /api/projects/{id}`: Get project details.
- `POST /api/projects`: Create project.
- `PUT /api/projects/{id}`: Update project.
- `DELETE /api/projects/{id}`: Delete project.
- `GET /api/projects/{id}/members`: List all members assigned to project.
- `POST /api/projects/{id}/members/{employeeId}`: Add employee to project.
- `DELETE /api/projects/{id}/members/{employeeId}`: Remove employee from project.

### 4. Users (`/api/customer`)
- `GET /api/customer`: List all employees/managers/admins.
- `GET /api/customer/{id}`: Get user by ID.
- `POST /api/customer`: Create user.
- `PUT /api/customer/{id}`: Update user.
- `DELETE /api/customer/{id}`: Safely unlink subordinates/projects and delete user.

### 5. Reports (`/api/report`)
- `GET /api/report/project/{projectId}/pdf?timePeriod={LAST_WEEK|LAST_MONTH|LAST_YEAR}`: Generate project PDF availability report based on approved leave requests covering the period.

---

## Business Logic & State Machine

1. **Leave Day Calculation:**
   - Evaluated as working days (Monday through Friday only, excluding weekends).
2. **Balance Deduction:**
   - Approving a request (`PENDING/EDITED -> APPROVED`) atomically deducts requested working days from the balance.
   - Cancelling an approved request (`APPROVED -> CANCELLED`) restores the deducted working days.
3. **Allowed Status Transitions:**
   - `PENDING` / `EDITED` $\rightarrow$ `APPROVED`, `REJECTED`, or `CANCELLED`.
   - `APPROVED` $\rightarrow$ `CANCELLED`.
   - `REJECTED` and `CANCELLED` are terminal states.
4. **Error Handling (RFC 7807):**
   - Violations return structured `ProblemDetail` responses with `title`, `status`, `detail`, and `timestamp` (e.g. `400 Bad Request`, `404 Not Found`, `409 Conflict`).

---

## Testing

```bash
mvn test
```
