/* climate */

CREATE TABLE IF NOT EXISTS climate
(
    id BIGSERIAL PRIMARY KEY,
    climate_updated_at TIMESTAMP NOT NULL,
    humidity real NOT NULL,
    temperature real NOT NULL
);

/* stock */

CREATE TABLE IF NOT EXISTS stock
(
    id BIGSERIAL PRIMARY KEY,
    stock_updated_at TIMESTAMP NOT NULL,
    stock_value real NOT NULL
);


/* workinghours */

CREATE TABLE IF NOT EXISTS customer
(
	id BIGSERIAL PRIMARY KEY,
	name TEXT,
	contact TEXT
);

CREATE TABLE IF NOT EXISTS rfidtag
(
	id BIGSERIAL PRIMARY KEY,
	rfid_uid_hex TEXT,
	rfid_uid BIGINT,
	user_id BIGINT NOT NULL,
	tag_type TEXT,
	valid_until TIMESTAMP,
	deactivated BOOLEAN,
	info TEXT
);

CREATE TABLE IF NOT EXISTS users
(
	id BIGSERIAL PRIMARY KEY,
	current_rfid_tag_id BIGINT,
	deactivated BOOLEAN,
	name TEXT NOT NULL,
	given_name TEXT NOT NULL,  
 	password TEXT NOT NULL,	
	email TEXT NOT NULL,
	customer_id BIGINT NOT NULL,
	role TEXT,
	FOREIGN KEY (current_rfid_tag_id) REFERENCES rfidtag (id),
	FOREIGN KEY (customer_id) REFERENCES customer (id)
);

ALTER TABLE rfidtag DROP CONSTRAINT IF EXISTS user_fk;

ALTER TABLE rfidtag 
ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE IF NOT EXISTS workevent
(
	id BIGSERIAL PRIMARY KEY,
	rfid_tag_id BIGINT NOT NULL,
	event_time TIMESTAMP NOT NULL,
	client_info TEXT NOT NULL,
	FOREIGN KEY (rfid_tag_id) REFERENCES rfidtag (id)	
);

CREATE TABLE IF NOT EXISTS workperiod
(
	id BIGSERIAL PRIMARY KEY,
	rfid_uid BIGINT NOT NULL,
	work_date TIMESTAMP NOT NULL,
	work_start TIMESTAMP NOT NULL,
	work_finish TIMESTAMP,
	work_duration_seconds BIGINT,
	start_event_id BIGINT NOT NULL,
	finish_event_id BIGINT,
	FOREIGN KEY (start_event_id) REFERENCES workevent (id),
	FOREIGN KEY (finish_event_id) REFERENCES workevent (id)
);

CREATE TABLE IF NOT EXISTS rfidtaginuse
(
	id BIGSERIAL PRIMARY KEY,
	rfid_uid BIGINT NOT NULL,
	work_period_id BIGINT NOT NULL,
	FOREIGN KEY (work_period_id) REFERENCES workperiod (id)
);