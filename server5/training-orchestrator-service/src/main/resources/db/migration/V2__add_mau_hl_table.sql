CREATE TABLE IF NOT EXISTS mau_hl (
    id SERIAL PRIMARY KEY,
    thong_tin_hl_id INTEGER NOT NULL REFERENCES training_session(id) ON DELETE CASCADE,
    thong_tin_mau_json TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_mau_hl_thong_tin_hl_id
    ON mau_hl (thong_tin_hl_id, id);
