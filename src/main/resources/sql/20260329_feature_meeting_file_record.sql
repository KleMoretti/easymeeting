-- EasyMeeting feature patch (2026-03-29)
-- 会议文件记录（用于文件上传下载与消息附件关联）
CREATE TABLE IF NOT EXISTS meeting_file_record (
  file_id VARCHAR(32) NOT NULL,
  meeting_id VARCHAR(32) NOT NULL,
  upload_user_id VARCHAR(32) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(512) NOT NULL,
  file_size BIGINT NOT NULL,
  file_type INT NOT NULL,
  file_suffix VARCHAR(20) DEFAULT NULL,
  create_time DATETIME NOT NULL,
  PRIMARY KEY (file_id),
  KEY idx_file_meeting_create_time (meeting_id, create_time),
  KEY idx_file_upload_user_id (upload_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
