-- SQLite migration: V2__init_sqlite_cache.sql
-- Tạo cache table cho training sessions
-- Được sync từ PostgreSQL via RabbitMQ

CREATE TABLE IF NOT EXISTS training_session_cache (
    id INTEGER PRIMARY KEY,
    tracking_id TEXT NOT NULL UNIQUE,
    trang_thai TEXT NOT NULL,
    epochs INTEGER NOT NULL,
    batch_size INTEGER NOT NULL,
    learning_rate REAL NOT NULL,
    kich_thuoc_anh INTEGER NOT NULL,
    loai_thiet_bi TEXT NOT NULL,
    early_stopping_patience INTEGER NOT NULL,
    optimizer TEXT NOT NULL,
    current_epoch INTEGER,
    do_chinh_xac REAL,
    do_nhay REAL,
    bat_dau_luc TEXT,
    ket_thuc_luc TEXT,
    duong_dan_mo_hinh_ket_qua TEXT,
    mo_hinh_hl_json TEXT,
    phien_ban_hl_json TEXT,
    ds_mau_hl_json TEXT,
    synced_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sync_version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cache_tracking_id ON training_session_cache(tracking_id);
CREATE INDEX IF NOT EXISTS idx_cache_trang_thai ON training_session_cache(trang_thai);
CREATE INDEX IF NOT EXISTS idx_cache_synced_at ON training_session_cache(synced_at DESC);
