ALTER TABLE workevent DROP COLUMN IF EXISTS rfid_tag_id;

ALTER TABLE workperiod DROP COLUMN IF EXISTS rfid_uid;

/* Drop Table RfidInUse */
DROP TABLE IF EXISTS rfidinuse;
