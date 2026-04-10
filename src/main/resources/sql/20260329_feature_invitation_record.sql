-- EasyMeeting feature patch (2026-03-29)
-- 会议邀请记录（用于邀请去重、邀请历史追踪、邀请状态回写）
CREATE TABLE IF NOT EXISTS meeting_invite_record (
  invite_id VARCHAR(32) NOT NULL,
  meeting_id VARCHAR(32) NOT NULL,
  meeting_no VARCHAR(32) NOT NULL,
  meeting_name VARCHAR(100) NOT NULL,
  invite_user_id VARCHAR(32) NOT NULL,
  receive_user_id VARCHAR(32) NOT NULL,
  invite_message VARCHAR(255) DEFAULT NULL,
  status INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  deal_time DATETIME DEFAULT NULL,
  PRIMARY KEY (invite_id),
  KEY idx_invite_receive_status (receive_user_id, status, create_time),
  KEY idx_invite_meeting_receive_status (meeting_id, receive_user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- status: PENDING=0, ACCEPT=1, REJECT=2, CANCEL=3
