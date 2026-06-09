-- Local PostgreSQL bootstrap.
-- Run with a superuser, for example:
-- psql -U postgres -f init-db.sql
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'notif_user') THEN
        CREATE ROLE notif_user WITH LOGIN PASSWORD 'notif_password';
    END IF;
END
$$;

SELECT 'CREATE DATABASE notification_db OWNER notif_user'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification_db')\gexec

\connect notification_db

-- ShedLock table for distributed scheduling
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_at TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    PRIMARY KEY (name)
);

CREATE INDEX IF NOT EXISTS idx_shedlock_lock_at ON shedlock(lock_at);

-- Delivery attempts tracking table
CREATE TABLE IF NOT EXISTS delivery_attempts (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_number INTEGER NOT NULL,
    message_id VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_event_id ON delivery_attempts(event_id);
CREATE INDEX IF NOT EXISTS idx_user_id ON delivery_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_channel ON delivery_attempts(channel);
CREATE INDEX IF NOT EXISTS idx_status ON delivery_attempts(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON delivery_attempts(created_at);
CREATE INDEX IF NOT EXISTS idx_delivery_event_channel ON delivery_attempts(event_id, channel);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    template_id VARCHAR(255) NOT NULL,
    channels TEXT NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_event_type ON notifications(event_type);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);

-- Templates table
CREATE TABLE IF NOT EXISTS templates (
    id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    version INTEGER DEFAULT 1,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_templates_event_type ON templates(event_type);
CREATE INDEX IF NOT EXISTS idx_templates_active ON templates(active);

