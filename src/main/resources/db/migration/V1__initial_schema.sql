-- V1__initial_schema.sql: Initial schema for Salon Manager

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. USERS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_email ON users(email);

-- 2. EMPLOYEES
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,
    role_title VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    default_commission_rate NUMERIC(5, 2) NOT NULL CHECK (default_commission_rate >= 0 AND default_commission_rate <= 100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_employees_cpf ON employees(cpf);
CREATE INDEX idx_employees_active ON employees(active);

-- 3. CLIENTS
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_clients_name ON clients(name);
CREATE INDEX idx_clients_phone ON clients(phone);

-- 4. SERVICES
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    custom_commission_rate NUMERIC(5, 2) CHECK (custom_commission_rate IS NULL OR (custom_commission_rate >= 0 AND custom_commission_rate <= 100)),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_services_active ON services(active);

-- 5. APPOINTMENTS
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    service_id UUID NOT NULL REFERENCES services(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    estimated_price NUMERIC(12, 2) NOT NULL CHECK (estimated_price >= 0),
    status VARCHAR(30) NOT NULL,
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_appointments_employee_period ON appointments (employee_id, start_time, end_time);
CREATE INDEX idx_appointments_client ON appointments (client_id);
CREATE INDEX idx_appointments_status ON appointments (status);

-- 6. MONTHLY CLOSINGS
CREATE TABLE monthly_closings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_year INTEGER NOT NULL,
    reference_month INTEGER NOT NULL CHECK (reference_month BETWEEN 1 AND 12),
    gross_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0,
    extra_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_expenses NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_commissions NUMERIC(12, 2) NOT NULL DEFAULT 0,
    net_result NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    closed_at TIMESTAMPTZ NOT NULL,
    closed_by VARCHAR(150) NOT NULL,
    audit_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_closing_year_month UNIQUE (reference_year, reference_month)
);

-- 7. MONTHLY CLOSING ITEMS
CREATE TABLE monthly_closing_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monthly_closing_id UUID NOT NULL REFERENCES monthly_closings(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id),
    total_services_count INTEGER NOT NULL DEFAULT 0,
    total_gross_generated NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_commission_due NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_closing_items_employee ON monthly_closing_items(employee_id);

-- 8. COMMISSIONS
CREATE TABLE commissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID UNIQUE NOT NULL REFERENCES appointments(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    monthly_closing_id UUID REFERENCES monthly_closings(id),
    applied_rate NUMERIC(5, 2) NOT NULL,
    service_amount NUMERIC(12, 2) NOT NULL,
    commission_amount NUMERIC(12, 2) NOT NULL,
    salon_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_commissions_employee ON commissions(employee_id);
CREATE INDEX idx_commissions_closing ON commissions(monthly_closing_id);
CREATE INDEX idx_commissions_status ON commissions(status);

-- 9. FINANCIAL TRANSACTIONS
CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID REFERENCES appointments(id),
    monthly_closing_id UUID REFERENCES monthly_closings(id),
    description VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fin_trans_date_type ON financial_transactions (transaction_date, type, status);
CREATE INDEX idx_fin_trans_closing ON financial_transactions (monthly_closing_id);
