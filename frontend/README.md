# Time Off Manager — Frontend

Modern Single Page Application (SPA) built with Angular 19 for the Time Off Manager platform.

---

## Tech Stack & Architecture

- **Framework:** Angular 19.2 (Standalone Components, Signals, Control Flow `@if` / `@for`)
- **Language:** TypeScript 5.7+
- **Styling:** SCSS, responsive CSS grid/flexbox
- **State Management:** Angular Signals & Services (`SessionService`, `ApiService`, `ToastService`)
- **Authentication:** JWT Bearer token authentication via `AuthInterceptor` and functional route guards (`roleGuard`)
- **HTTP Client:** Angular `HttpClient` using relative URLs (`/api/...`)
- **Development Proxy:** Angular CLI dev server configured via `proxy.conf.json` to proxy `/api` calls to `http://localhost:8080`
- **Production Serving:** Nginx container reverse-proxying `/api` directly to backend container (same-origin, no CORS)

---

## Prerequisites

- **Node.js 20+ (LTS)**
- **npm** (bundled with Node.js)

---

## Setup & Running Locally

### 1. Install Dependencies

```bash
npm install
```

### 2. Start the Development Server

```bash
npm start
```
*(Executes `ng serve --proxy-config proxy.conf.json`)*

- Navigate to [http://localhost:4200/](http://localhost:4200/).
- Source file changes trigger automatic hot reload.
- Requests to `/api/*` are forwarded to the local backend on `http://localhost:8080/api/*`.

---

## Application Pages & Modules

- **Login (`/login`):** Email and password authentication with "Forgot Password" self-service recovery link.
- **Password Reset (`/reset-password`):** One-time token password reset page opened from email links.
- **Overview (`/home`):** Dashboard showing remaining allowances, days reserved by pending requests, recent submissions, and quick actions.
- **New Request (`/new-request`):** Leave submission form with real-time working days calculation (Mon-Fri) and balance availability checks.
- **Request History (`/request-history`):** Tabular log of personal requests with sorting, filtering, edit, and cancel actions.
- **Projects & Reports (`/projects`):** Project roster management, `minEmp` staffing requirements, team assignment, and PDF report downloads.
- **Approvals (`/approvals`):** Manager/Admin queue for pending and modified leave requests with project staffing (`minEmp`) shortage alerts.
- **Administration (`/administration`):** Admin-only user management (account creation, role assignments, soft delete, password resets) and allowance allocation.
- **Team Absence Calendar (`/calendar`):** Monthly absence calendar with leave-type color codes, pending indicators, and daily staffing coverage meters.

---

## Available NPM Scripts

- `npm start`: Starts the local dev server on port `4200` with the API proxy.
- `npm run build`: Generates optimized production build in `dist/frontend/browser`.
- `npm run watch`: Builds and watches for changes in development mode.
- `npm test`: Runs unit tests via Karma and Jasmine.
