CREATE TABLE IF NOT EXISTS training_session (
    id SERIAL PRIMARY KEY,
    tracking_id VARCHAR(64) NOT NULL UNIQUE,
    epochs INTEGER NOT NULL,
    batch_size INTEGER NOT NULL,
    trang_thai VARCHAR(32) NOT NULL,
    bat_dau_luc TIMESTAMPTZ NULL,
    ket_thuc_luc TIMESTAMPTZ NULL,
    do_chinh_xac DOUBLE PRECISION NULL,
    do_nhay DOUBLE PRECISION NULL,
    current_epoch INTEGER NULL,
    learning_rate DOUBLE PRECISION NOT NULL,
    kich_thuoc_anh INTEGER NOT NULL,
    loai_thiet_bi VARCHAR(32) NOT NULL,
    early_stopping_patience INTEGER NOT NULL,
    optimizer VARCHAR(64) NOT NULL,
    phien_ban_hl_json TEXT NOT NULL,
    mo_hinh_hl_json TEXT NOT NULL,
    ds_mau_hl_json TEXT NOT NULL,
    duong_dan_mo_hinh_ket_qua TEXT NULL
);

CREATE TABLE IF NOT EXISTS training_status_event (
    id BIGSERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL REFERENCES training_session(id) ON DELETE CASCADE,
    tracking_id VARCHAR(64) NOT NULL,
    state VARCHAR(32) NOT NULL,
    detail TEXT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    current_epoch INTEGER NULL,
    precision DOUBLE PRECISION NULL,
    recall DOUBLE PRECISION NULL,
    log_line TEXT NULL,
    model_artifact_path TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_training_status_event_tracking_id
    ON training_status_event (tracking_id, updated_at, id);

CREATE INDEX IF NOT EXISTS idx_training_status_event_session_id
    ON training_status_event (session_id);
