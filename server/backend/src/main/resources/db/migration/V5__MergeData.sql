-- Table: workevent. Add userid for each entry
UPDATE workevent
	SET user_id=rf.user_id
	FROM (SELECT id, user_id FROM rfidtag) AS rf
	WHERE workevent.rfid_tag_id=rf.id;
	
-- Table: workperiod. Add userid for each entry
UPDATE workperiod
	SET user_id=rf.user_id
	FROM (SELECT rfid_uid, user_id FROM rfidtag) AS rf
	WHERE workperiod.rfid_uid=rf.rfid_uid;
	
-- Table: startedworkperiod. Add entry for each entry in legacy table: rfidinuse
UPDATE startedworkperiod
	SET id=rf.id, 
		work_period_id=rf.work_period_id, 
		user_id=rf.user_id
	FROM (SELECT r.id, r.work_period_id, w.user_id 
		  FROM rfidtaginuse r 
		  	JOIN workperiod w ON r.rfid_uid = w.rfid_uid) as rf;