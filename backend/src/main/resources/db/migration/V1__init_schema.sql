-- =========================================================================
-- LifeLink Smart Blood Bank Network & Hospital Coordination System
-- Flyway Database Migration: V1__init_schema.sql
-- =========================================================================

-- 1. Facilities (Hospitals & Blood Banks)
CREATE TABLE IF NOT EXISTS lifelink_facilities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL, -- 'HOSPITAL' or 'BLOOD_BANK'
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED'
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(20) NOT NULL,
    contact_phone VARCHAR(50) NOT NULL,
    contact_email VARCHAR(150) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    operating_hours VARCHAR(100),
    emergency_contact VARCHAR(100),
    approval_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ll_facility_type_status ON lifelink_facilities(type, status);
CREATE INDEX IF NOT EXISTS idx_ll_facility_city ON lifelink_facilities(city);

-- 2. Users
CREATE TABLE IF NOT EXISTS lifelink_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL, -- 'HOSPITAL_USER', 'BLOOD_BANK_USER', 'NETWORK_ADMIN', 'AUDITOR'
    facility_id BIGINT REFERENCES lifelink_facilities(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    designation VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ll_users_email ON lifelink_users(email);
CREATE INDEX IF NOT EXISTS idx_ll_users_facility ON lifelink_users(facility_id);
CREATE INDEX IF NOT EXISTS idx_ll_users_role ON lifelink_users(role);

-- 3. Blood Inventory Batches
CREATE TABLE IF NOT EXISTS lifelink_blood_inventory_batches (
    id BIGSERIAL PRIMARY KEY,
    facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    batch_number VARCHAR(100) NOT NULL,
    blood_group VARCHAR(20) NOT NULL, -- 'A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE'
    component_type VARCHAR(50) NOT NULL, -- 'WHOLE_BLOOD', 'RED_BLOOD_CELLS', 'PLATELETS', 'FRESH_FROZEN_PLASMA', 'CRYOPRECIPITATE'
    quantity INT NOT NULL CHECK (quantity >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    dispatched_quantity INT NOT NULL DEFAULT 0 CHECK (dispatched_quantity >= 0),
    collection_date TIMESTAMP WITH TIME ZONE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'QUARANTINED', 'EXPIRED', 'DEPLETED'
    storage_location VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_ll_facility_batchnum UNIQUE (facility_id, batch_number),
    CONSTRAINT chk_ll_batch_quantities CHECK (quantity >= (reserved_quantity + dispatched_quantity))
);

CREATE INDEX IF NOT EXISTS idx_ll_batches_facility ON lifelink_blood_inventory_batches(facility_id);
CREATE INDEX IF NOT EXISTS idx_ll_batches_matching ON lifelink_blood_inventory_batches(status, blood_group, component_type, expiry_date);

-- 4. Inventory Transactions (Authoritative Audit Log of Stock Adjustments)
CREATE TABLE IF NOT EXISTS lifelink_inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES lifelink_blood_inventory_batches(id) ON DELETE CASCADE,
    facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL, -- 'COLLECTION', 'ALLOCATION', 'RELEASE', 'DISPATCH', 'QUARANTINE', 'ADJUSTMENT'
    quantity_changed INT NOT NULL,
    resulting_available_quantity INT NOT NULL,
    reference_type VARCHAR(50), -- 'REQUEST', 'RESERVATION', 'DISPATCH', 'MANUAL_AUDIT'
    reference_id BIGINT,
    actor_id BIGINT REFERENCES lifelink_users(id) ON DELETE SET NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ll_inv_tx_batch ON lifelink_inventory_transactions(batch_id);
CREATE INDEX IF NOT EXISTS idx_ll_inv_tx_facility ON lifelink_inventory_transactions(facility_id);

-- 5. Blood Requests
CREATE TABLE IF NOT EXISTS lifelink_blood_requests (
    id BIGSERIAL PRIMARY KEY,
    request_reference VARCHAR(50) NOT NULL UNIQUE,
    hospital_facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    requester_user_id BIGINT NOT NULL REFERENCES lifelink_users(id) ON DELETE RESTRICT,
    blood_group VARCHAR(20) NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    units_requested INT NOT NULL CHECK (units_requested > 0),
    units_fulfilled INT NOT NULL DEFAULT 0 CHECK (units_fulfilled >= 0),
    urgency VARCHAR(50) NOT NULL DEFAULT 'ROUTINE', -- 'ROUTINE', 'URGENT', 'CRITICAL'
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED', -- 'DRAFT', 'SUBMITTED', 'MATCHING', 'AWAITING_BANK_RESPONSES', 'PARTIALLY_FULFILLED', 'ACCEPTED', 'IN_TRANSFER', 'FULFILLED', 'UNFULFILLED', 'CANCELLED', 'REJECTED'
    required_by TIMESTAMP WITH TIME ZONE NOT NULL,
    patient_reference_code VARCHAR(100),
    clinical_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ll_requests_hospital ON lifelink_blood_requests(hospital_facility_id);
CREATE INDEX IF NOT EXISTS idx_ll_requests_status ON lifelink_blood_requests(status);
CREATE INDEX IF NOT EXISTS idx_ll_requests_urgency ON lifelink_blood_requests(urgency);

-- 6. Blood Request Responses (from Blood Banks)
CREATE TABLE IF NOT EXISTS lifelink_blood_request_responses (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES lifelink_blood_requests(id) ON DELETE CASCADE,
    blood_bank_facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ACCEPTED', 'PARTIALLY_ACCEPTED', 'REJECTED', 'WITHDRAWN'
    offered_units INT NOT NULL DEFAULT 0,
    notes TEXT,
    responded_by_user_id BIGINT REFERENCES lifelink_users(id) ON DELETE SET NULL,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ll_req_resp_pair UNIQUE (request_id, blood_bank_facility_id)
);

CREATE INDEX IF NOT EXISTS idx_ll_responses_request ON lifelink_blood_request_responses(request_id);
CREATE INDEX IF NOT EXISTS idx_ll_responses_bank ON lifelink_blood_request_responses(blood_bank_facility_id);

-- 7. Stock Reservations
CREATE TABLE IF NOT EXISTS lifelink_stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    reservation_reference VARCHAR(50) NOT NULL UNIQUE,
    request_id BIGINT NOT NULL REFERENCES lifelink_blood_requests(id) ON DELETE RESTRICT,
    batch_id BIGINT NOT NULL REFERENCES lifelink_blood_inventory_batches(id) ON DELETE RESTRICT,
    blood_bank_facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    units_reserved INT NOT NULL CHECK (units_reserved > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'CONFIRMED', 'EXPIRED', 'RELEASED', 'DISPATCHED'
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ll_reservations_batch ON lifelink_stock_reservations(batch_id);
CREATE INDEX IF NOT EXISTS idx_ll_reservations_request ON lifelink_stock_reservations(request_id);
CREATE INDEX IF NOT EXISTS idx_ll_reservations_status ON lifelink_stock_reservations(status);

-- 8. Blood Transfers
CREATE TABLE IF NOT EXISTS lifelink_blood_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_reference VARCHAR(50) NOT NULL UNIQUE,
    request_id BIGINT NOT NULL REFERENCES lifelink_blood_requests(id) ON DELETE RESTRICT,
    reservation_id BIGINT NOT NULL REFERENCES lifelink_stock_reservations(id) ON DELETE RESTRICT,
    source_facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    destination_facility_id BIGINT NOT NULL REFERENCES lifelink_facilities(id) ON DELETE RESTRICT,
    units_transferred INT NOT NULL CHECK (units_transferred > 0),
    status VARCHAR(50) NOT NULL DEFAULT 'PREPARED', -- 'PREPARED', 'DISPATCHED', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED'
    dispatcher_user_id BIGINT REFERENCES lifelink_users(id) ON DELETE SET NULL,
    receiver_user_id BIGINT REFERENCES lifelink_users(id) ON DELETE SET NULL,
    courier_details VARCHAR(255),
    temperature_log_summary VARCHAR(100),
    dispatched_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ll_transfers_source ON lifelink_blood_transfers(source_facility_id);
CREATE INDEX IF NOT EXISTS idx_ll_transfers_dest ON lifelink_blood_transfers(destination_facility_id);
CREATE INDEX IF NOT EXISTS idx_ll_transfers_status ON lifelink_blood_transfers(status);

-- 9. Notifications
CREATE TABLE IF NOT EXISTS lifelink_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_user_id BIGINT REFERENCES lifelink_users(id) ON DELETE CASCADE,
    facility_id BIGINT REFERENCES lifelink_facilities(id) ON DELETE CASCADE,
    target_role VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO', -- 'INFO', 'URGENT', 'ALERT', 'CONFIRMATION'
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ll_notifications_user ON lifelink_notifications(recipient_user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_ll_notifications_facility ON lifelink_notifications(facility_id, is_read);

-- 10. Audit Logs
CREATE TABLE IF NOT EXISTS lifelink_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(150),
    user_role VARCHAR(50),
    facility_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ll_audit_entity ON lifelink_audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_ll_audit_timestamp ON lifelink_audit_logs(timestamp);

-- 11. Refresh Tokens
CREATE TABLE IF NOT EXISTS lifelink_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES lifelink_users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ll_refresh_token_user ON lifelink_refresh_tokens(user_id);
