-- EasyMeeting feature patch (2026-03-29)
-- 1) 联系人关系
CREATE TABLE IF NOT EXISTS user_contact (
  user_id VARCHAR(32) NOT NULL,
  contact_id VARCHAR(32) NOT NULL,
  status INT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  PRIMARY KEY (user_id, contact_id),
  KEY idx_user_contact_user_id (user_id),
  KEY idx_user_contact_contact_id (contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 联系人申请
CREATE TABLE IF NOT EXISTS user_contact_apply (
  apply_id VARCHAR(32) NOT NULL,
  apply_user_id VARCHAR(32) NOT NULL,
  receive_user_id VARCHAR(32) NOT NULL,
  apply_message VARCHAR(255) DEFAULT NULL,
  status INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  deal_time DATETIME DEFAULT NULL,
  PRIMARY KEY (apply_id),
  KEY idx_contact_apply_receive_status (receive_user_id, status),
  KEY idx_contact_apply_apply_receive (apply_user_id, receive_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) 预约会议不需要新增字段，复用 meeting_info.status：
-- RUNNING=0, FINISHED=1, RESERVED=2
