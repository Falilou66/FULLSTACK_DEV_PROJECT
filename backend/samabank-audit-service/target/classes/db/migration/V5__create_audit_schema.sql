CREATE TABLE IF NOT EXISTS audit_events (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type      VARCHAR(100) NOT NULL,
    actor_id        UUID         NOT NULL,
    actor_role      VARCHAR(20)  NOT NULL,
    resource_type   VARCHAR(50)  NOT NULL,
    resource_id     UUID,
    correlation_id  UUID         NOT NULL,
    ip_address      VARCHAR(45),
    channel         VARCHAR(20),
    payload         JSONB        NOT NULL DEFAULT '{}',
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    sequence_num    BIGSERIAL    NOT NULL
);

CREATE OR REPLACE FUNCTION prevent_audit_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_events is immutable — UPDATE/DELETE forbidden';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_audit_immutable
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_modification();

CREATE INDEX IF NOT EXISTS idx_audit_actor       ON audit_events(actor_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_resource    ON audit_events(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_correlation ON audit_events(correlation_id);
CREATE INDEX IF NOT EXISTS idx_audit_type        ON audit_events(event_type, occurred_at DESC);
