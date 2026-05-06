DO $$
DECLARE
    session_row RECORD;
    sample_item JSONB;
    parsed JSONB;
BEGIN
    FOR session_row IN
        SELECT ts.id, ts.ds_mau_hl_json
        FROM training_session ts
        WHERE NOT EXISTS (
            SELECT 1
            FROM mau_hl mh
            WHERE mh.thong_tin_hl_id = ts.id
        )
    LOOP
        BEGIN
            IF session_row.ds_mau_hl_json IS NULL OR btrim(session_row.ds_mau_hl_json) = '' THEN
                CONTINUE;
            END IF;

            parsed := session_row.ds_mau_hl_json::jsonb;
            IF jsonb_typeof(parsed) <> 'array' THEN
                CONTINUE;
            END IF;

            FOR sample_item IN
                SELECT value
                FROM jsonb_array_elements(parsed)
            LOOP
                INSERT INTO mau_hl (thong_tin_hl_id, thong_tin_mau_json)
                VALUES (session_row.id, sample_item::text);
            END LOOP;
        EXCEPTION
            WHEN others THEN
                RAISE WARNING 'Skip backfill for training_session.id=% due to invalid ds_mau_hl_json', session_row.id;
                CONTINUE;
        END;
    END LOOP;
END
$$;
