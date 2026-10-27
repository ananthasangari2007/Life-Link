# LifeLink — Security Architecture & Production Hardening Policy

> **System:** LifeLink — Smart Blood Bank Network & Hospital Coordination System  
> **Classification:** Healthcare Coordination Infrastructure (Non-diagnostic / Non-clinical Release)

---

## 1. Security Architecture Overview

LifeLink connects hospital clinical teams and regional blood banking centers. Due to the sensitive nature of emergency healthcare logistics and facility inventories, security is implemented in-depth across network, application, database, and operational tiers.

```
┌─────────────────────────────────────────────────────────────┐
│                       Client Browser                        │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTPS / TLS 1.3
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway & CORS                       │
│  - Origin Whitelist Verification (Strict Domain Binding)    │
│  - Security Headers (nosniff, DENY frame-options, XSS)      │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Spring Security Filter Chain                │
│  - Stateless Session Management (No JSESSIONID)             │
│  - JwtAuthenticationFilter (HMAC-SHA256 Token Extraction)   │
│  - Role-Based Access Control (RBAC Hierarchy)               │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Domain & Business Logic                   │
│  - IDOR Facility Ownership Verification                     │
│  - Concurrency Safety: Pessimistic Row Locking              │
│  - State Machine Transition Validation                      │
│  - Audit Logging (Non-repudiable Event History)             │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Aiven PostgreSQL Storage                    │
│  - Mandatory SSL Encryption (`sslmode=require`)             │
│  - Namespace Collision Defense (`lifelink_*` tables)        │
│  - Flyway Versioned Migrations (Clean Disabled)             │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Authentication & Identity Management

### 2.1 Stateless JSON Web Tokens (JWT)
- **Token Format:** Signed JWT using `io.jsonwebtoken (JJWT 0.12.6)`.
- **Signature Algorithm:** HMAC-SHA256 with minimum 256-bit secret key.
- **Expiration Policy:**
  - Access Token: 24 hours (`APP_JWT_EXPIRATION_MS=86400000`).
  - Refresh Token: 7 days (`APP_JWT_REFRESH_EXPIRATION_MS=604800000`), persisted in `lifelink_refresh_tokens` and revocable upon logout.
- **Storage:** Frontend stores tokens in `localStorage` and transmits via `Authorization: Bearer <token>` HTTP header.

### 2.2 Password Security & Hashing
- **Algorithm:** BCrypt with a workload/strength factor of 12 (`BCryptPasswordEncoder`).
- **Storage:** Plaintext passwords are never persisted to disk, written to logs, or serialized in API responses.
- **DTO Isolation:** Entity-to-DTO mappers (`UserSummaryDto`) explicitly exclude password hashes.

---

## 3. Authorization & Role-Based Access Control (RBAC)

LifeLink enforces strict least-privilege role boundaries:

| Role | Permitted Actions | Restricted Actions |
|---|---|---|
| `SYSTEM_ADMIN` | Network setup, facility registration, user administration, global audit inspection, system diagnostics. | Cannot modify medical inventories on behalf of blood banks. |
| `HOSPITAL_COORDINATOR` | Create blood requests, view matching blood banks, accept/cancel allocations, track inbound transfers. | Cannot view or modify inventory batches belonging to other facilities. |
| `BLOOD_BANK_STAFF` | Manage own blood bank batches, reserve/dispatch units, accept/reject matching requests. | Cannot create hospital requests or alter external bank inventory. |
| `LOGISTICS_COORDINATOR`| Update blood transit statuses, record transfer tracking numbers, record temperature logs. | Cannot change batch quantities or alter hospital request requirements. |
| `AUDITOR` | Read-only inspection of inventory logs, request lifecycles, and security audit logs. | Zero mutation permissions across all tables. |

### 3.1 IDOR (Insecure Direct Object Reference) Prevention
All inventory mutations, request submissions, and transfer acceptances verify that the authenticated user's `facilityId` matches the facility associated with the record:
```java
// Example from InventoryServiceImpl.java
if (!user.getFacility().getId().equals(batch.getFacility().getId()) 
    && user.getRole() != UserRole.SYSTEM_ADMIN) {
    throw new UnauthorizedFacilityAccessException(
        "User not authorized to manage inventory for facility: " + batch.getFacility().getName());
}
```

---

## 4. Concurrency Safety & Double-Allocation Prevention

In high-concurrency emergency scenarios, multiple hospitals might attempt to reserve the same blood batch simultaneously:

### 4.1 Database-Level Pessimistic Locking
When an allocation or reservation is requested, LifeLink acquires an exclusive write lock on the inventory row in PostgreSQL:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM BloodInventoryBatch b WHERE b.id = :id")
Optional<BloodInventoryBatch> findByIdWithPessimisticLock(@Param("id") UUID id);
```
- Competing transactions queue at the database level.
- `BloodInventoryBatch.reserve(units)` atomically verifies `getAvailableUnits() >= units`.
- If insufficient units remain, transaction rolls back and throws `InsufficientInventoryException` before any transfer record is created.

### 4.2 Optimistic Locking via JPA `@Version`
All entities inheriting from [`BaseEntity`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/entity/BaseEntity.java) maintain an integer `@Version` column. Any concurrent out-of-band updates trigger an immediate `OptimisticLockException`.

---

## 5. Transport Security & Network Protection

### 5.1 Enforced TLS / SSL
- All external API communications require HTTPS (TLS 1.2 / TLS 1.3).
- PostgreSQL database connection enforces SSL:
  ```
  jdbc:postgresql://pg-3f7545bd-ananthasangari2007-2330.j.aivencloud.com:21859/defaultdb?sslmode=require
  ```
  Insecure fallback (`sslmode=disable`) is blocked.

### 5.2 Cross-Origin Resource Sharing (CORS)
- Uncontrolled wildcard origins (`*`) are disallowed in production.
- Permitted origins are explicitly bound via `APP_CORS_ALLOWED_ORIGINS` (e.g. `https://lifelink-network.vercel.app`).
- Preflight `OPTIONS` requests are cached for 1 hour to reduce overhead.

### 5.3 Security Headers
Configured via Spring Security and Vercel edge configuration:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY` (Clickjacking defense)
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`

---

## 6. Audit Logging & Non-Repudiation

LifeLink records every security-sensitive and clinical action into the immutable [`lifelink_audit_logs`](file:///d:/LifeLink/backend/src/main/resources/db/migration/V1__init_schema.sql) table:

- **Recorded Attributes:**
  - `action`: e.g. `CREATE_REQUEST`, `RESERVE_INVENTORY`, `DISPATCH_TRANSFER`, `LOGIN_SUCCESS`, `UNAUTHORIZED_ACCESS_ATTEMPT`.
  - `entity_type`: Target domain object (`BloodRequest`, `BloodInventoryBatch`, etc.).
  - `entity_id`: UUID of the affected entity.
  - `facility_id`: Associated facility.
  - `user_id` & `username`: Actor identity.
  - `ip_address`: Source IP address.
  - `metadata`: JSON payload containing context (e.g., requested units, status changes).
  - `created_at`: Non-updatable UTC timestamp.

Auditors and administrators can review audit logs in real time through the **Auditor View** dashboard.

---

## 7. Responsible-Use Medical Disclaimer

> **IMPORTANT CLINICAL & LEGAL NOTICE:**  
> LifeLink is a software coordination and logistics communication tool designed to assist healthcare institutions in discovering potential blood stock availability and tracking transfer status across participating facilities.  
> 
> LifeLink **DOES NOT** replace qualified blood-bank personnel, professional clinical judgment, laboratory testing, serological crossmatching, pathogen screening, or authorized medical release procedures. No biological product may be transfused solely on the basis of electronic records in this system without adherence to established institutional and national clinical transfusion safety protocols.
