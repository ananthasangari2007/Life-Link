# LifeLink — Cloud Deployment & Production Hosting Guide

This guide details the step-by-step procedure to deploy the **LifeLink** platform live to production using:
- **Backend:** Render Web Service (Java 17/21 Spring Boot 3 multi-stage Docker container)
- **Frontend:** Vercel (React 19 + TypeScript + Vite SPA)
- **Database:** Aiven Managed PostgreSQL (`pg-3f7545bd-ananthasangari2007-2330.j.aivencloud.com:21859`)
- **Source Control:** GitHub

---

## 1. Architecture & Live Topology

```
   ┌────────────────────────────────────────────────────────────┐
   │             User Browser / Mobile Device                   │
   └───────────────┬────────────────────────────┬───────────────┘
                   │ (HTTPS)                    │ (HTTPS / SSE)
                   ▼                            ▼
   ┌───────────────────────────────┐ ┌───────────────────────────┐
   │       Vercel (Frontend)       │ │   Render (Java Backend)   │
   │  React 19 + Vite + Tailwind   │ │  Spring Boot 3.3.5 / JDK  │
   │  SPA Routing + Static CDN     │ │  REST APIs + SSE Stream   │
   └───────────────────────────────┘ └─────────────┬─────────────┘
                                                   │ (TLS 1.3 / SSL)
                                                   ▼
                                     ┌───────────────────────────┐
                                     │     Aiven PostgreSQL      │
                                     │  `defaultdb` (lifelink_*) │
                                     └───────────────────────────┘
```

---

## 2. Aiven Database Configuration & Safety Guarantee

LifeLink uses your existing managed Aiven PostgreSQL database instance.

### Safe Migration Protocol
- **Zero Collision Guarantee:** All LifeLink application tables are strictly prefixed with `lifelink_` (e.g., `lifelink_facilities`, `lifelink_inventory_batches`, `lifelink_requests`, `lifelink_transfers`).
- **Independent Migration History:** Flyway records migrations into `flyway_schema_history_lifelink`.
- **Non-Destructive Operations:** Hibernate `ddl-auto` is set to `validate` in production. Tables and indexes are created exclusively via forward-only versioned SQL migrations (`V1__init_schema.sql`). No tables, columns, or records belonging to other projects are dropped or truncated.
- **Connection Details:**
  - **Host:** `pg-3f7545bd-ananthasangari2007-2330.j.aivencloud.com`
  - **Port:** `21859`
  - **Database:** `defaultdb`
  - **User:** `avnadmin`
  - **SSL Mode:** `require` (Strict TLS encryption)
  - **JDBC URL:** `jdbc:postgresql://pg-3f7545bd-ananthasangari2007-2330.j.aivencloud.com:21859/defaultdb?sslmode=require`

---

## 3. Step 1: Git Repository Setup & Push to GitHub

1. Ensure all development artifacts, node modules, and credentials are excluded:
   - Root `.gitignore` already ignores `.env`, `*.key`, `backend/target/`, and `frontend/node_modules/`.
2. Initialize and push your repository:
   ```bash
   # From the d:\LifeLink root folder
   git init
   git add .
   git commit -m "feat: complete LifeLink full-stack platform with Java OOP backend and Vite frontend"

   # Create repository on GitHub (e.g. https://github.com/<your-username>/lifelink.git)
   git branch -M main
   git remote add origin https://github.com/<your-username>/lifelink.git
   git push -u origin main
   ```

---

## 4. Step 2: Deploy Backend to Render

### Method A: Deploy via `render.yaml` Blueprint (Fastest)
1. Log in to [Render Dashboard](https://dashboard.render.com).
2. Click **New +** -> **Blueprint**.
3. Connect your GitHub repository.
4. Render detects [`render.yaml`](file:///d:/LifeLink/render.yaml) at the repository root.
5. In the configuration screen, supply your secret variables:
   - `SPRING_DATASOURCE_PASSWORD`: *(Enter your real Aiven PostgreSQL `avnadmin` password)*
   - `APP_ADMIN_PASSWORD`: *(Enter a secure password for the bootstrap admin, e.g. `Admin@LifeLink2026!`)*
6. Click **Apply**. Render will automatically build the multi-stage Docker container and deploy the service.

### Method B: Deploy Manually as a Web Service
1. Click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Configure the service:
   - **Name:** `lifelink-backend`
   - **Region:** Frankfurt, Oregon, or closest to your database
   - **Branch:** `main`
   - **Root Directory:** *(leave blank)*
   - **Runtime:** `Docker`
   - **Dockerfile Path:** `backend/Dockerfile`
   - **Docker Context:** `backend`
   - **Plan:** Free or Starter
4. Add the following **Environment Variables**:
   | Key | Value | Notes |
   |---|---|---|
   | `SPRING_PROFILES_ACTIVE` | `prod` | Activates production database & validation settings |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://pg-3f7545bd-ananthasangari2007-2330.j.aivencloud.com:21859/defaultdb?sslmode=require` | Aiven connection URL |
   | `SPRING_DATASOURCE_USERNAME` | `avnadmin` | Database username |
   | `SPRING_DATASOURCE_PASSWORD` | `<your-aiven-password>` | Set to your real Aiven password |
   | `DB_POOL_SIZE` | `5` | Keeps connections within Aiven free tier limits |
   | `APP_JWT_SECRET` | `c2VjdXJlLWxvbmdlci1rZXktZm9yLWxpZmVsaW5rLTI1Ni1iaXQtaG1hYy1zaGEyNTYtMjAyNg==` | At least 32 characters / 256 bits |
   | `APP_JWT_EXPIRATION_MS` | `86400000` | 24 hours |
   | `APP_BOOTSTRAP_ADMIN_ENABLED` | `true` | Seeds initial admin account on first start |
   | `APP_ADMIN_EMAIL` | `admin@lifelink.network` | Initial admin login email |
   | `APP_ADMIN_PASSWORD` | `Admin@LifeLink2026!` | Initial admin login password |
   | `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,https://<your-vercel-domain>.vercel.app` | Allow Vercel frontend |
5. Set Health Check path to: `/actuator/health`
6. Click **Create Web Service**.

Render will build and run the Spring Boot app. Once finished, note your public URL:
`https://lifelink-backend.onrender.com`

---

## 5. Step 3: Deploy Frontend to Vercel

1. Log in to [Vercel](https://vercel.com).
2. Click **Add New...** -> **Project**.
3. Import your GitHub repository.
4. Configure the project settings:
   - **Framework Preset:** `Vite`
   - **Root Directory:** Click **Edit** and choose `frontend`
   - **Build Command:** `npm run build`
   - **Output Directory:** `dist`
   - **Install Command:** `npm install`
5. Under **Environment Variables**, add:
   | Key | Value |
   |---|---|
   | `VITE_API_BASE_URL` | `https://lifelink-backend.onrender.com/api/v1` |
   *(Replace with your actual Render backend URL)*
6. Click **Deploy**.
7. Vercel will install dependencies, compile TypeScript, run the Vite build, and publish the site.
8. Once complete, copy your Vercel public URL (e.g., `https://lifelink-network.vercel.app`).
9. **Important:** Return to Render Dashboard -> `lifelink-backend` -> **Environment Variables**, and ensure `APP_CORS_ALLOWED_ORIGINS` includes your live Vercel URL so CORS requests are allowed.

---

## 6. Verification & First-Time Initialization

### 1. Verify Backend Health
Navigate to:
```
https://lifelink-backend.onrender.com/actuator/health
```
Expected response:
```json
{
  "status": "UP"
}
```

### 2. Verify Swagger / OpenAPI Documentation
Navigate to:
```
https://lifelink-backend.onrender.com/swagger-ui.html
```
Explore the interactive REST API endpoints for authentication, facilities, inventory, matching, requests, transfers, and analytics.

### 3. Log In & Seed Demo Network
1. Open your live Vercel frontend URL in the browser.
2. Click **Sign In**.
3. Use the bootstrap administrator credentials:
   - **Email:** `admin@lifelink.network`
   - **Password:** `Admin@LifeLink2026!`
4. On the Admin Dashboard, click **"Seed Demo Network"** (or use the Demo Credentials Modal on the landing page).
5. The backend will populate:
   - 4 accredited hospitals (City General, Metro Trauma, St. Jude Children's, Memorial University)
   - 5 regional blood banks (Central Blood Center, Red Cross Regional, Lifeline Blood Services, Valley Blood Bank, Harbor Blood Institute)
   - 25+ verified blood inventory batches with realistic expiry dates and component types
   - Realistic initial requests and active transfers.
6. Use the **Quick Switch** accounts on the demo credentials modal to test:
   - **Hospital Coordinator:** `hospital@lifelink.network` (Password: `Hospital@2026!`)
   - **Blood Bank Manager:** `bank@lifelink.network` (Password: `BloodBank@2026!`)
   - **Auditor / Compliance Officer:** `auditor@lifelink.network` (Password: `Auditor@2026!`)

---

## 7. Troubleshooting

| Symptom | Probable Cause | Resolution |
|---|---|---|
| Render build fails with out-of-memory | Maven package using default heap | Dockerfile already sets `-Xms256m -Xmx512m`. Ensure Render instance has at least 512MB RAM. |
| Backend logs `Connection refused` or SSL error | Missing `sslmode=require` | Check that JDBC URL has `?sslmode=require` at the end. |
| Frontend displays `CORS error` in console | Vercel domain not whitelisted | Update `APP_CORS_ALLOWED_ORIGINS` in Render environment variables with your exact Vercel URL. |
| 404 on page refresh on Vercel | Missing SPA routing rewrite | Verified: `frontend/vercel.json` contains `rewrites: [{ "source": "/(.*)", "destination": "/index.html" }]`. |
