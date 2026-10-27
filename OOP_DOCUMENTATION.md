# LifeLink — Object-Oriented Programming (OOP) Architecture Documentation

> **Course Project:** Java Object-Oriented Programming (OOP) & Enterprise Systems Design  
> **Application:** LifeLink — Smart Blood Bank Network & Hospital Coordination System  
> **Platform:** Java 17/21 LTS, Spring Boot 3.3.5, Hibernate/JPA, PostgreSQL  

---

## 1. Executive Summary & Design Philosophy

LifeLink was designed and implemented strictly following clean **Object-Oriented Programming (OOP)** principles and enterprise architectural design patterns. Instead of treating entities as mere passive data holders (anemic domain model) and centralizing procedural logic in monolithic controllers, LifeLink implements a rich domain model where business invariants, state transitions, validation rules, and polymorphic algorithms are encapsulated within dedicated domain objects, policies, and service interfaces.

### Core Architectural Layers
```
┌─────────────────────────────────────────────────────────────┐
│                 Presentation Layer (REST)                   │
│   AuthController, BloodRequestController, Inventory...      │
└──────────────────────────────┬──────────────────────────────┘
                               │ (Calls Interfaces)
┌──────────────────────────────▼──────────────────────────────┐
│                  Application / Service Layer                │
│   InventoryService, BloodMatchingService, TransferService   │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
┌──────────────▼──────────────┐┌──────────────▼───────────────┐
│     Domain Models & State   ││   Polymorphic Strategies     │
│   BloodInventoryBatch,      ││   StandardAboRhCompatibility │
│   BloodRequest, Transfer    ││   FifoExpiryAllocation       │
└──────────────┬──────────────┘└──────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────────┐
│               Data Access & Concurrency Layer               │
│   Pessimistic Locking (@Lock), Optimistic Locking (@Version)│
│   PostgreSQL persistence via Spring Data JPA                │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Pillar 1: Encapsulation & Rich Domain Modeling

### 2.1 Private State & Controlled Invariant Mutations
In LifeLink, internal entity state is strictly guarded. Entities do not expose open setters that allow arbitrary external mutation of critical balances or statuses.

* **Class:** [`BloodInventoryBatch`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/entity/BloodInventoryBatch.java)
  - Fields such as `unitsRemaining`, `unitsReserved`, `status`, and `expiryDate` are `private`.
  - The available stock is a derived calculation:
    ```java
    public int getAvailableUnits() {
        return Math.max(0, this.unitsRemaining - this.unitsReserved);
    }
    ```
  - Direct modification is replaced with explicit domain methods that enforce business invariants:
    - `reserve(int units)`: Validates that requested units are positive, that the batch is not expired, that its status is `AVAILABLE`, and that available units satisfy the request. Atomically increments `unitsReserved`. If remaining units equal reserved units, automatically updates status to `RESERVED`.
    - `releaseReservation(int units)`: Releases previously reserved units, ensuring `unitsReserved` never drops below zero, and restores `AVAILABLE` status.
    - `dispatch(int units)`: Fulfills a blood transfer. Decrements both `unitsRemaining` and `unitsReserved`. If `unitsRemaining` reaches 0, sets status to `DEPLETED`.
    - `markExpired()`: Updates batch status to `EXPIRED` if current date is past `expiryDate`.
  - Attempting to reserve or dispatch invalid quantities throws domain exceptions (`InsufficientInventoryException`, `InvalidStateTransitionException`).

### 2.2 Domain Enums with Embedded Business Behavior
Enums in LifeLink are not merely string lists; they encapsulate domain logic and clinical compatibility rules:

* **Enum:** [`BloodGroup`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/enums/BloodGroup.java)
  - Contains ABO and Rh factor properties: `aboType` ("A", "B", "AB", "O") and `rhPositive` (boolean).
  - Encapsulates red blood cell and plasma compatibility methods directly:
    - `canDonateRbcTo(BloodGroup recipient)`: O- is universal RBC donor; AB+ is universal RBC recipient.
    - `canReceiveRbcFrom(BloodGroup donor)`
    - `canDonatePlasmaTo(BloodGroup recipient)`: AB is universal plasma donor; O is universal plasma recipient.
    - `canReceivePlasmaFrom(BloodGroup donor)`
* **Enum:** [`ComponentType`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/enums/ComponentType.java)
  - Encapsulates clinical shelf-life rules and storage requirements:
    - `WHOLE_BLOOD` (35 days, 2-6°C)
    - `PACKED_RED_CELLS` (42 days, 1-6°C)
    - `FRESH_FROZEN_PLASMA` (365 days, -18°C or colder)
    - `PLATELETS` (5 days, 20-24°C with continuous agitation)
    - `CRYOPRECIPITATE` (365 days, -18°C or colder)
  - Provides `calculateExpiryDate(LocalDate collectionDate)` directly on the enum.
* **Enum:** [`RequestUrgency`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/enums/RequestUrgency.java)
  - Encapsulates escalation SLAs: `CRITICAL` (15 mins), `URGENT` (60 mins), `ROUTINE` (240 mins).

---

## 3. Pillar 2: Inheritance & Abstract Base Classes

### 3.1 Base Entity Mapping
* **Class:** [`BaseEntity`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/domain/entity/BaseEntity.java)
  - Marked with `@MappedSuperclass` to provide shared state and behavior to all persistent domain entities:
    - `UUID id`: Primary key generated via UUIDv4.
    - `LocalDateTime createdAt`: Timestamp managed automatically via JPA `@PrePersist`.
    - `LocalDateTime updatedAt`: Timestamp managed automatically via JPA `@PreUpdate`.
    - `Long version`: `@Version` optimistic concurrency lock counter preventing silent lost-update anomalies.
  - Implements uniform identity semantics (`equals` and `hashCode`) based on persistent identity.

### 3.2 Custom Domain Exception Hierarchy
Rather than throwing generic `RuntimeException`, LifeLink features a clean domain exception hierarchy rooted at an abstract base class:
```
Throwable
  └── Exception
        └── RuntimeException
              └── LifeLinkException (abstract base with HttpStatus & errorCode)
                    ├── ResourceNotFoundException (404 NOT_FOUND)
                    ├── InvalidStateTransitionException (409 CONFLICT)
                    ├── InsufficientInventoryException (409 CONFLICT)
                    ├── DoubleReservationException (409 CONFLICT)
                    ├── UnauthorizedFacilityAccessException (403 FORBIDDEN)
                    └── BusinessRuleException (422 UNPROCESSABLE_ENTITY)
```
* **Class:** [`LifeLinkException`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/exception/LifeLinkException.java)
  - Declares abstract methods/fields for HTTP status code and machine-readable error codes.
  - Intercepted globally by [`GlobalExceptionHandler`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/exception/GlobalExceptionHandler.java), transforming typed Java exceptions into RFC 7807 compliant JSON `ErrorResponse` objects.

---

## 4. Pillar 3: Polymorphism & Design Patterns

### 4.1 Strategy Pattern: Blood Compatibility Engine
* **Interface:** [`BloodCompatibilityPolicy`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/matching/BloodCompatibilityPolicy.java)
  - Declares contract:
    ```java
    boolean isCompatible(BloodGroup donorGroup, BloodGroup recipientGroup, ComponentType componentType);
    Set<BloodGroup> getCompatibleDonorGroups(BloodGroup recipientGroup, ComponentType componentType);
    ```
* **Implementation:** [`StandardAboRhCompatibilityPolicy`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/matching/StandardAboRhCompatibilityPolicy.java)
  - Implements clinical transfusion compatibility rules across different component types.
  - Red Blood Cells follow standard RBC rules (O- is universal donor).
  - Plasma products (Fresh Frozen Plasma) invert the rules (AB is universal plasma donor, O can only receive O).
  - Platelets prioritize exact matches followed by ABO-compatible donors.
  - *Extensibility:* The system can adopt alternative matching policies (e.g., pediatric restricted policies or extended phenotype Kell/Duffy matching) by registering a new `BloodCompatibilityPolicy` bean without altering the matching service.

### 4.2 Strategy Pattern: Inventory Allocation (FEFO)
* **Interface:** [`InventoryAllocationStrategy`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/matching/InventoryAllocationStrategy.java)
* **Implementation:** [`FifoExpiryAllocationStrategy`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/matching/FifoExpiryAllocationStrategy.java)
  - Implements First-Expiring, First-Out (FEFO / FIFO) allocation.
  - Prioritizes batches that are closest to expiration (while still safe) to minimize clinical blood wastage.
  - Sorts candidate batches deterministically: `expiryDate ASC`, `createdAt ASC`, `id ASC`.

### 4.3 Observer / Notification Channel Strategy
* **Interface:** [`NotificationChannel`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/notification/NotificationChannel.java)
  - Defines `send(Notification notification)`.
  - Implementations:
    - [`InAppNotificationChannel`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/notification/InAppNotificationChannel.java): Stores notification in database and dispatches live event via SSE emitter.
    - [`EmailNotificationChannel`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/notification/EmailNotificationChannel.java): Formats and queues external dispatch.

---

## 5. Pillar 4: Abstraction & Separation of Concerns

### 5.1 Service Layer Abstraction
All controllers interact exclusively with high-level service interfaces rather than concrete implementations:
- `InventoryService` (implemented by `InventoryServiceImpl`)
- `BloodMatchingService` (implemented by `BloodMatchingServiceImpl`)
- `ReservationService` (implemented by `ReservationServiceImpl`)
- `RequestCoordinationService` (implemented by `RequestCoordinationServiceImpl`)
- `TransferService` (implemented by `TransferServiceImpl`)
- `FacilityService` (implemented by `FacilityServiceImpl`)
- `UserService` (implemented by `UserServiceImpl`)
- `AnalyticsService` (implemented by `AnalyticsServiceImpl`)

### 5.2 Deterministic Finite State Machines
Request and transfer lifecycles are modeled as explicit state machines with validated transition graphs:
* **Validator:** [`RequestStateValidator`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/state/RequestStateValidator.java)
  - Validates legal transitions for `RequestStatus`:
    `PENDING` -> `SEARCHING` -> `MATCHED` / `PARTIALLY_MATCHED` -> `ALLOCATED` -> `IN_TRANSIT` -> `FULFILLED` (or `CANCELLED` / `EXPIRED`).
  - Throws `InvalidStateTransitionException` if an unauthorized or illegal transition is attempted.
* **Validator:** [`TransferStateValidator`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/service/state/TransferStateValidator.java)
  - Validates legal transitions for `TransferStatus`:
    `INITIATED` -> `DISPATCHED` -> `IN_TRANSIT` -> `DELIVERED` -> `ACCEPTED` (or `REJECTED`).

---

## 6. Concurrency Control & Thread Safety

### 6.1 Pessimistic Database Locking
To guarantee zero duplicate blood reservations when multiple hospitals request units from the same blood bank simultaneously:
* **Repository:** [`BloodInventoryBatchRepository`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/repository/BloodInventoryBatchRepository.java)
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT b FROM BloodInventoryBatch b WHERE b.id = :id")
  Optional<BloodInventoryBatch> findByIdWithPessimisticLock(@Param("id") UUID id);
  ```
* When `ReservationServiceImpl.reserveBatch()` executes within a `@Transactional` boundary, it acquires an exclusive `SELECT ... FOR UPDATE` lock on the specific batch record in PostgreSQL. Competing transactions wait until the lock is released, preventing double-allocation and race conditions.

### 6.2 Optimistic Locking
`BaseEntity` includes `@Version Long version`. Any concurrent modifications outside pessimistic locks trigger Hibernate `OptimisticLockException`, preventing lost updates.

---

## 7. Generics & Collections

* **Generic API Envelope:** [`ApiResponse<T>`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/dto/common/ApiResponse.java) provides a type-safe, consistent envelope for all REST responses:
  ```java
  public class ApiResponse<T> {
      private boolean success;
      private String message;
      private T data;
      private LocalDateTime timestamp;
  }
  ```
* **Generic Pagination Envelope:** [`PageResponse<T>`](file:///d:/LifeLink/backend/src/main/java/com/lifelink/dto/common/PageResponse.java) safely wraps Spring Data `Page<T>` for cross-tier serialization.
* **Collections Usage:** Java Collections (`List`, `Set`, `Map`, `Collections.unmodifiableList`) are utilized throughout matching algorithms, filtering streams, and grouping batches by blood group and facility.

---

## 8. Summary Checklist of OOP Concepts in LifeLink

| OOP Principle / Design Pattern | Exact LifeLink Class / Interface | Purpose & Implementation |
|---|---|---|
| **Encapsulation** | `BloodInventoryBatch` | Private fields; state modifications only via `reserve()`, `releaseReservation()`, `dispatch()`. |
| **Encapsulation** | `BloodGroup`, `ComponentType` | Rich enums with clinical compatibility rules & shelf life calculations. |
| **Inheritance** | `BaseEntity` | Base class for `id`, `createdAt`, `updatedAt`, `@Version`. |
| **Inheritance** | `LifeLinkException` | Abstract root of typed domain exception hierarchy. |
| **Polymorphism** | `BloodCompatibilityPolicy` | Interface implemented by `StandardAboRhCompatibilityPolicy`. |
| **Polymorphism** | `InventoryAllocationStrategy` | Interface implemented by `FifoExpiryAllocationStrategy` (FEFO). |
| **Polymorphism** | `NotificationChannel` | In-app SSE emitter channel & external email notification channel. |
| **Abstraction** | Service interfaces | Full decoupling between REST Controllers and persistence services. |
| **State Machine** | `RequestStateValidator` | Explicit state transition tables validating request lifecycles. |
| **Concurrency (Pessimistic Lock)** | `BloodInventoryBatchRepository` | `@Lock(LockModeType.PESSIMISTIC_WRITE)` prevents double reservation. |
| **Generics** | `ApiResponse<T>`, `PageResponse<T>` | Strongly typed response wrappers across the API. |
| **Composition** | `BloodRequest`, `BloodTransfer` | Composed of facilities, enums, reservations, and audit logs. |
