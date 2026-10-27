# LifeLink — Smart Blood Bank Network & Hospital Coordination System

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20LTS-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%20(Aiven)-4169E1?logo=postgresql&logoColor=white)](https://aiven.io/)
[![React](https://img.shields.io/badge/React-19%20%7C%20TypeScript-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-CSS%204-06B6D4?logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Deployment](https://img.shields.io/badge/Deploy-Render%20%2B%20Vercel-black?logo=vercel&logoColor=white)](https://vercel.com)

> **LifeLink** is a centralized healthcare coordination platform connecting hospitals, regional blood centers, and logistics coordinators to eliminate emergency blood shortages through automated multi-bank matching, FEFO-aware inventory management, pessimistic concurrency reservation, and end-to-end transfer tracking.

---

## 🌟 Key Capabilities

- 🩸 **Intelligent Multi-Bank Matching Engine:** Evaluates clinical ABO/Rh component compatibility (Whole Blood, Packed Red Cells, Fresh Frozen Plasma, Platelets, Cryoprecipitate) across multiple blood banks in a single search, providing split-fulfillment options when no single bank can satisfy urgent demand.
- 🔒 **Zero-Duplicate Reservation Guarantee:** Utilizes PostgreSQL pessimistic write locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to eliminate race conditions and double-allocation when multiple hospitals seek the same units simultaneously.
- ⏳ **First-Expiring First-Out (FEFO) Inventory:** Prioritizes batches approaching expiry while strictly excluding expired units, preventing critical biological wastage.
- 🚦 **Validated Lifecycle State Machines:** Formal transition models for Requests (`PENDING` -> `MATCHING` -> `ACCEPTED` -> `IN_TRANSFER` -> `FULFILLED`) and Transfers (`INITIATED` -> `DISPATCHED` -> `DELIVERED` -> `ACCEPTED`).
- ⚡ **Live Real-Time Streaming:** Server-Sent Events (SSE) push instant notifications to dashboards on reservation updates, emergency escalations, and dispatch status without aggressive polling.
- 📊 **Network Analytics & Compliance Audit Trail:** Real-time metrics on network availability, fulfillment rates, and an immutable log capturing every clinical reservation, dispatch, and access attempt.
- 🧪 **One-Click Demo Network Seeder:** Instantly populates realistic simulated facilities (4 hospitals, 5 regional blood banks, 25+ inventory batches, and sample emergency requests) for presentation and academic evaluation.

---

## 🏛️ System Architecture

```
                    ┌───────────────────────────────┐
                    │      React 19 + TypeScript    │
                    │   Vite + Tailwind UI (Vercel) │
                    └───────────────┬───────────────┘
                                    │ (HTTPS / JSON / SSE)
                                    ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                 Spring Boot 3.3.5 Backend (Render / Docker)               │
│                                                                           │
│  ┌─────────────────────────┐  ┌────────────────────────────────────────┐  │
│  │   REST Controllers      │  │  Spring Security (Stateless JWT)       │  │
│  │  Auth, Inventory, Match │  │  Role-Based Access Control (RBAC)      │  │
│  └────────────┬────────────┘  └───────────────────┬────────────────────┘  │
│               │                                   │                       │
│  ┌────────────▼───────────────────────────────────▼────────────────────┐  │
│  │                         Service Layer                               │  │
│  │  - BloodMatchingService (Polymorphic ABO/Rh & Split Plans)          │  │
│  │  - ReservationService (Pessimistic Concurrency & Auto-Expiry)       │  │
│  │  - RequestCoordinationService & TransferService (State Machines)   │  │
│  │  - SseNotificationService (Live Emitter Stream)                    │  │
│  └────────────┬───────────────────────────────────┬────────────────────┘  │
│               │                                   │                       │
│  ┌────────────▼────────────┐         ┌────────────▼────────────────────┐  │
│  │   Rich Domain Models    │         │     Polymorphic Strategies      │  │
│  │  Encapsulated Batches,  │         │  - BloodCompatibilityPolicy     │  │
│  │  Requests, Transfers    │         │  - FifoExpiryAllocationStrategy │  │
│  └────────────┬────────────┘         └────────────┬────────────────────┘  │
└───────────────┼───────────────────────────────────┼───────────────────────┘
                │                                   │
                ▼                                   ▼
┌───────────────────────────────────────────────────────────────────────────┐
│               Managed PostgreSQL Database (Aiven Cloud)                   │
│  - Namespaced Tables: `lifelink_*` (Collision-resistant)                   │
│  - Migrations: Flyway (`flyway_schema_history_lifelink`)                  │
│  - Transport: Enforced SSL Encryption (`sslmode=require`)                 │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## ☕ Object-Oriented Programming (OOP) Highlights

This project was built from the ground up to embody core OOP and enterprise architectural patterns:

| Concept | Concrete LifeLink Implementation | Reference |
|---|---|---|
| **Encapsulation** | `BloodInventoryBatch` guards all internal fields; updates occur exclusively through domain methods `reserve()`, `releaseReservation()`, `dispatch()`. | [`BloodInventoryBatch.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/entity/BloodInventoryBatch.java) |
| **Domain Enums** | `BloodGroup` embeds ABO/Rh compatibility formulas (`canDonateRbcTo()`, `canReceivePlasmaFrom()`); `ComponentType` encapsulates shelf-life calculations. | [`BloodGroup.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/enums/BloodGroup.java) |
| **Inheritance** | `BaseEntity` mapped superclass encapsulates `@Id`, `createdAt`, `updatedAt`, and `@Version` optimistic locking. | [`BaseEntity.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/entity/BaseEntity.java) |
| **Polymorphism** | `BloodCompatibilityPolicy` interface implemented by `StandardAboRhCompatibilityPolicy`; `InventoryAllocationStrategy` interface implemented by `FifoExpiryAllocationStrategy`. | [`BloodMatchingServiceImpl.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/matching/BloodMatchingServiceImpl.java) |
| **Abstraction** | Strict separation of API controllers from service contracts and Spring Data repositories. | [`ReservationService.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/reservation/ReservationService.java) |
| **Concurrency** | Row-level `@Lock(LockModeType.PESSIMISTIC_WRITE)` in repository layer prevents concurrent double-allocations. | [`BloodInventoryBatchRepository.java`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/repository/BloodInventoryBatchRepository.java) |

👉 **Read the comprehensive OOP design documentation:** [`OOP_DOCUMENTATION.md`](file:///d:/LifeLink/OOP_DOCUMENTATION.md)

---

## 🚀 Quick Start Guide (Local Development)

### Prerequisites
- **Java:** JDK 17 or JDK 21 installed (`java -version`)
- **Maven:** 3.9+ or included Maven Wrapper
- **Node.js:** 18+ and npm (`node -v`, `npm -v`)

### 1. Run the Backend
```bash
# Navigate to backend directory
cd backend

# Option A: Run using local in-memory H2 database (PostgreSQL mode)
./mvnw spring-boot:run

# Option B: Run against Aiven PostgreSQL instance
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod -Dspring-boot.run.arguments="--SPRING_DATASOURCE_PASSWORD=your_aiven_password"
```
The backend starts on `http://localhost:8080`.
- **Health Check:** `http://localhost:8080/actuator/health`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

### 2. Run the Frontend
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (already installed)
npm install

# Start Vite development server
npm run dev
```
The frontend starts on `http://localhost:5173`. Requests to `/api/*` are automatically proxied to `http://localhost:8080`.

---

## 👥 Demo Accounts & Testing Persona Matrix

LifeLink comes with pre-configured accounts for role-specific testing:

| Role | Email | Password | Primary Actions |
|---|---|---|---|
| **System Administrator** | `admin@lifelink.network` | `Admin@LifeLink2026!` | Seed demo data, register facilities, manage accounts, view global audits. |
| **Hospital Coordinator** | `hospital@lifelink.network` | `Hospital@2026!` | Search multi-bank blood stock, submit emergency requests, track inbound shipments. |
| **Blood Bank Manager** | `bank@lifelink.network` | `BloodBank@2026!` | Add and inspect blood batches, approve or reject reservations, dispatch transfers. |
| **Compliance Auditor** | `auditor@lifelink.network` | `Auditor@2026!` | Real-time immutable audit trail inspection, compliance verification. |

> 💡 **Tip:** Use the **"Demo Credentials"** modal in the web interface to switch between accounts with a single click.

---

## ☁️ Production Deployment

The project is pre-configured for one-click deployment:
- **Backend on Render:** Multi-stage Docker build configured in [`backend/Dockerfile`](file:///d:/LifeLink/backend/Dockerfile) and [`render.yaml`](file:///d:/LifeLink/render.yaml).
- **Frontend on Vercel:** Single Page Application routing rewrite and security headers in [`frontend/vercel.json`](file:///d:/LifeLink/frontend/vercel.json).
- **Database on Aiven:** Production profile in [`application-prod.yml`](file:///d:/LifeLink/backend/src/main/resources/application-prod.yml) with safe namespaced migrations.

👉 **Follow the complete deployment walkthrough:** [`DEPLOYMENT.md`](file:///d:/LifeLink/DEPLOYMENT.md)

---

## 🛡️ Security & Concurrency Defense

- **HMAC-SHA256 JWT** authentication with separate short-lived access and revocable refresh tokens.
- **BCrypt** password hashing (strength factor 12).
- **Role-Based Access Control (RBAC)** and IDOR facility boundary verification.
- **Pessimistic Database Locking** for race-condition-free reservations.
- Mandatory **TLS / SSL** for all external traffic and database connections.

👉 **Read the full security documentation:** [`SECURITY.md`](file:///d:/LifeLink/SECURITY.md)

---

## ⚠️ Responsible-Use Medical Disclaimer

> **CLINICAL NOTICE:**  
> LifeLink is an administrative coordination and logistics software tool designed to aid healthcare institutions in discovering potential blood stock availability and tracking transfer status.  
> 
> LifeLink **DOES NOT** replace qualified blood-bank personnel, professional clinical judgment, laboratory testing, serological crossmatching, pathogen screening, or authorized medical release procedures. No biological product may be transfused solely on the basis of electronic records in this system without adherence to established clinical transfusion safety protocols.

---

## 📄 License
This project is developed for educational and academic evaluation purposes.
