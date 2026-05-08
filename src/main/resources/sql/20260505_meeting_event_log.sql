CREATE TABLE IF NOT EXISTS meeting_event_log (
  event_id VARCHAR(32) NOT NULL,
  message_id BIGINT NOT NULL,
  meeting_id VARCHAR(32) DEFAULT NULL,
  user_id VARCHAR(32) DEFAULT NULL,
  event_type INT DEFAULT NULL,
  send_to_type INT DEFAULT NULL,
  receive_user_id VARCHAR(32) DEFAULT NULL,
  status INT NOT NULL DEFAULT 0,
  retry_count INT NOT NULL DEFAULT 0,
  error_reason VARCHAR(500) DEFAULT NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  PRIMARY KEY (event_id),
  UNIQUE KEY uk_event_message_id (message_id),
  KEY idx_event_meeting_status (meeting_id, status, create_time),
  KEY idx_event_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
