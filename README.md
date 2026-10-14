# Time Off Manager

A full-stack enterprise web application for managing employee time-off requests, team projects, leave balances, and availability reporting with role-based access control (Employee, Manager, Admin).

---

## Architecture & Tech Stack

- **Backend:**
  - Java 25
  - Spring Boot 3.5.6 (Spring Web, Spring Data JPA, Spring Validation)
  - MapStruct 1.6.3 (Entity-DTO mapping)
  - LibrePDF OpenPDF 2.0.3 (PDF report generation)
  - Springdoc OpenAPI 2.8.13 (Swagger UI documentation)
  - H2 Database (in-memory relational database for local development)
- **Frontend:**
  - Angular 19.2 (Standalone Components, Signals, Reactive Forms, SCSS)
  - TypeScript 5.7+
  - Development Proxy (`proxy.conf.json`) to forward `/api` requests to the backend
- **Deployment Ready:**
  - Containerizable via Docker & Docker Compose
  - Configured for AWS deployment

---

## Repository Structure

```text
wad_time_off_manager/
├── backend/
│   └── timeOffManager/            # Spring Boot application
│       ├── pom.xml
│       └── src/
│           ├── main/
│           │   ├── java/com/project/timeOffManager/
│           │   │   ├── config/      # CORS & Web MVC configuration
│           │   │   ├── controller/  # REST controllers
│           │   │   ├── dto/         # Request & Response DTOs
│           │   │   ├── entity/      # JPA entities (User, Request, Project, Balance)
│           │   │   ├── enums/       # Status, TimePeriod, Roles
│           │   │   ├── exception/   # ProblemDetail GlobalExceptionHandler
│           │   │   ├── mapper/      # MapStruct DTO mappers
│           │   │   ├── repository/  # Spring Data JPA repositories
│           │   │   └── service/     # Business logic & state machine
│           │   └── resources/
│           │       ├── application.yml
│           │       └── data.sql     # Seed data
│           └── test/
└── frontend/                      # Angular application
    ├── angular.json
    ├── package.json
    ├── proxy.conf.json            # Dev proxy for /api -> http://localhost:8080
    └── src/
        ├── environments/          # Environment configuration (dev / prod)
        ├── app/
        │   ├── components/        # Reusable UI components (header, sidebar)
        │   ├── pages/             # Page components (overview, new-request, history, projects, management)
        │   └── services/          # API & Session services
        └── main.ts
```

---

## Prerequisites (From a Bare Machine)

Ensure the following runtimes and tools are installed:

1. **Java Development Kit (JDK) 25**
   - Verify: `java -version`
2. **Apache Maven 3.9+**
   - Verify: `mvn -version`
3. **Node.js 20+ (LTS) & npm**
   - Verify: `node -v` and `npm -v`
4. **Git**
   - Verify: `git --version`

---

## Quick Start (From a Bare Cloned Repo)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd wadproj2/wad_time_off_manager
```

---

### 2. Run the Backend

Open a terminal and navigate to the backend directory:

```bash
cd backend/timeOffManager
```

#### Compile and verify build:
```bash
mvn clean test-compile
```

#### Start the Spring Boot Application:
```bash
mvn spring-boot:run
```

- **Backend URL:** `http://localhost:8080`
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: *(empty)*

---

### 3. Run the Frontend

Open a second terminal and navigate to the frontend directory:

```bash
cd frontend
```

#### Install dependencies:
```bash
npm install
```

#### Start the Angular Development Server (with API proxy):
```bash
npm start
```
*(This executes `ng serve --proxy-config proxy.conf.json`)*

- **Frontend URL:** [http://localhost:4200](http://localhost:4200)
- The frontend automatically reloads when source files are modified.
- Requests to `/api/*` are transparently forwarded by the dev server to `http://localhost:8080/api/*`.

---

## Building for Production

### Backend Build
```bash
cd backend/timeOffManager
mvn clean package -DskipTests
```
The executable JAR is generated in `target/timeOffManager-0.0.1-SNAPSHOT.jar`.

### Frontend Build
```bash
cd frontend
npm run build
```
Optimized static production assets are compiled to `frontend/dist/frontend/browser`.

---

## Running Automated Tests

### Backend Tests:
```bash
cd backend/timeOffManager
mvn test
```

### Frontend Unit Tests:
```bash
cd frontend
npm test
```