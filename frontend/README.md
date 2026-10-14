# Time Off Manager — Frontend

Modern Single Page Application (SPA) built with Angular 19 for the Time Off Manager platform.

---

## Tech Stack & Architecture

- **Framework:** Angular 19.2 (Standalone Components, Signals, Control Flow `@if` / `@for`)
- **Language:** TypeScript 5.7+
- **Styling:** SCSS, responsive CSS grid/flexbox
- **State Management:** Angular Signals & Services (`SessionService`, `ApiService`)
- **HTTP Client:** Angular `HttpClient` with relative URLs (`/api/...`)
- **Development Proxy:** Angular CLI dev server configured via `proxy.conf.json` to proxy `/api` calls to `http://localhost:8080`

---

## Prerequisites

- **Node.js 20+ (LTS)**
- **npm** (comes with Node.js)
- Angular CLI (`npm install -g @angular/cli@19` — optional, `npx ng` or npm scripts can be used directly)

---

## Setup & Running Locally

### 1. Install Dependencies

From this directory (`frontend`):

```bash
npm install
```

### 2. Start the Development Server

```bash
npm start
```
*(Runs `ng serve --proxy-config proxy.conf.json`)*

- Navigate to [http://localhost:4200/](http://localhost:4200/).
- The app automatically reloads when source files change.
- Requests to `/api/*` are automatically forwarded to `http://localhost:8080/api/*`.

---

## Environment & Proxy Configuration

- **Environment files:**
  - `src/environments/environment.ts` (development: `apiBase: ''`)
  - `src/environments/environment.prod.ts` (production: `apiBase: ''`)
- **Development Proxy (`proxy.conf.json`):**
  ```json
  {
    "/api": {
      "target": "http://localhost:8080",
      "secure": false,
      "changeOrigin": true
    }
  }
  ```
- **Production Routing:**
  In production, an Nginx reverse proxy or cloud router serves the static Angular bundle and routes `/api/*` traffic directly to the backend container, maintaining identical relative URLs in both environments without CORS issues.

---

## Available NPM Scripts

- `npm start`: Starts the local dev server on port `4200` with the API proxy.
- `npm run build`: Generates optimized production build in `dist/frontend/browser`.
- `npm run watch`: Builds and watches for changes in development configuration.
- `npm test`: Runs unit tests via Karma and Jasmine.

---

## Application Navigation & Pages

- **Overview (`/home`):** View remaining leave balances, pending requests, and upcoming team time off.
- **New Request (`/new-request`):** Submit a time-off request with interactive working-day preview and balance validation.
- **Request History (`/request-history`):** View past requests, edit pending requests, or cancel approved absences.
- **Projects (`/projects`):** Manage company projects, minimum staffing thresholds (`minEmp`), assigned employees, and PDF report downloads.
- **Management (`/management`):** Approver queue for managers/admins to review and approve/reject leave requests, plus user and allowance management.

