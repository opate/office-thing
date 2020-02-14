-- Add column userid to workevent. Remove column rfid_uid from workevent.
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE workevent ADD COLUMN user_id BIGINT;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column user_id already exists in workevent.';
        END;
END$$;

-- Add column userid to workperiod. Remove column rfid_uid from workperiod.
DO $$ 
    BEGIN
        BEGIN
            ALTER TABLE workperiod ADD COLUMN user_id BIGINT;
        EXCEPTION
            WHEN duplicate_column THEN RAISE NOTICE 'column user_id already exists in workperiod.';
        END;
    END$$;

-- Create startedworkperiod table instead of rfidinuse
CREATE TABLE IF NOT EXISTS startedworkperiod
(
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL,
	work_period_id BIGINT NOT NULL,
	FOREIGN KEY (work_period_id) REFERENCES workperiod (id)
);

-- Remove workevent constraints
ALTER TABLE workevent ALTER COLUMN rfid_tag_id DROP NOT NULL;
ALTER TABLE workperiod ALTER COLUMN rfid_uid DROP NOT NULL;
ALTER TABLE workperiod ALTER COLUMN work_date DROP NOT NULL;

