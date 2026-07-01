-- Initial schema for triage-api

CREATE TABLE IF NOT EXISTS users (
    user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name       VARCHAR(120)  NOT NULL,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    password_hash   TEXT          NOT NULL,
    preferred_lang  VARCHAR(10)   NOT NULL DEFAULT 'EN',
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS doctors (
    doctor_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name        VARCHAR(150)  NOT NULL,
    email            VARCHAR(255)  NOT NULL UNIQUE,
    password_hash    TEXT          NOT NULL,
    specialty        VARCHAR(120)  NOT NULL,
    license_number   VARCHAR(80)   NOT NULL,
    bio              TEXT,
    years_experience INTEGER,
    languages_spoken VARCHAR(100)  DEFAULT 'EN',
    status           VARCHAR(20)   NOT NULL DEFAULT 'OFFLINE',
    rating_average   DOUBLE PRECISION,
    rating_count     INTEGER       NOT NULL DEFAULT 0,
    max_active_chats INTEGER       NOT NULL DEFAULT 5,
    is_active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS symptom_sessions (
    session_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    started_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS symptom_entries (
    entry_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id    UUID         NOT NULL REFERENCES symptom_sessions(session_id) ON DELETE CASCADE,
    symptom_text  TEXT         NOT NULL,
    severity      INTEGER,
    duration_text VARCHAR(100),
    recorded_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS health_tips (
    tip_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(200)  NOT NULL,
    content       TEXT          NOT NULL,
    triage_level  VARCHAR(10)   NOT NULL,
    category      VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS triage_results (
    result_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          UUID         NOT NULL UNIQUE REFERENCES symptom_sessions(session_id) ON DELETE CASCADE,
    triage_level        VARCHAR(10)  NOT NULL,
    ai_confidence       DOUBLE PRECISION,
    probable_conditions TEXT,
    escalation_advice   TEXT,
    raw_ai_response     TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS result_tips (
    result_id  UUID NOT NULL REFERENCES triage_results(result_id) ON DELETE CASCADE,
    tip_id     UUID NOT NULL REFERENCES health_tips(tip_id) ON DELETE CASCADE,
    PRIMARY KEY (result_id, tip_id)
);

CREATE TABLE IF NOT EXISTS conversations (
    conversation_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id        UUID        NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    doctor_id         UUID        NOT NULL REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    linked_session_id UUID        REFERENCES symptom_sessions(session_id) ON DELETE SET NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    chief_complaint   TEXT,
    accepted_at       TIMESTAMPTZ,
    closed_at         TIMESTAMPTZ,
    patient_rating    INTEGER,
    patient_feedback  TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS messages (
    message_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id  UUID        NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_type      VARCHAR(10) NOT NULL,
    sender_id        UUID        NOT NULL,
    content          TEXT        NOT NULL,
    is_read          BOOLEAN     NOT NULL DEFAULT FALSE,
    sent_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_symptom_sessions_user_id   ON symptom_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_symptom_entries_session_id ON symptom_entries(session_id);
CREATE INDEX IF NOT EXISTS idx_conversations_patient_id   ON conversations(patient_id);
CREATE INDEX IF NOT EXISTS idx_conversations_doctor_id    ON conversations(doctor_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status       ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id   ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_is_read           ON messages(conversation_id, is_read);
CREATE INDEX IF NOT EXISTS idx_health_tips_triage_level   ON health_tips(triage_level);
