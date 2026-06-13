-- 陪练端后端：周排班模板、训练笔记、房间资料、证书扩展、报告维度、评价回复

CREATE TABLE IF NOT EXISTS ctx_coach_weekly_schedule (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  coach_id BIGINT UNSIGNED NOT NULL,
  day_of_week TINYINT NOT NULL COMMENT '0=周日 .. 6=周六',
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_coach_weekly (coach_id, day_of_week),
  CONSTRAINT fk_coach_weekly_coach FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_training_note (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  training_id BIGINT UNSIGNED NOT NULL,
  coach_id BIGINT UNSIGNED NOT NULL,
  note_type VARCHAR(32) NOT NULL DEFAULT 'HIGHLIGHT' COMMENT 'HIGHLIGHT/ISSUE',
  label VARCHAR(128) NULL,
  content TEXT NULL,
  start_sec INT NULL,
  end_sec INT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_training_note_training (training_id, created_at),
  CONSTRAINT fk_training_note_training FOREIGN KEY (training_id) REFERENCES ctx_training_record(id),
  CONSTRAINT fk_training_note_coach FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_room_material (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  room_id VARCHAR(128) NOT NULL,
  training_id BIGINT UNSIGNED NULL,
  uploader_id BIGINT UNSIGNED NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_room_material_room (room_id, created_at),
  CONSTRAINT fk_room_material_uploader FOREIGN KEY (uploader_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE ctx_coach_certificate
  ADD COLUMN verify_code VARCHAR(32) NULL COMMENT '学信网验证码' AFTER kind,
  ADD COLUMN title VARCHAR(128) NULL AFTER verify_code,
  ADD COLUMN reject_reason VARCHAR(255) NULL AFTER status;

ALTER TABLE ctx_training_report
  ADD COLUMN score_content TINYINT NULL COMMENT '1-5 内容价值' AFTER score_pressure,
  ADD COLUMN score_time TINYINT NULL COMMENT '1-5 时间把控' AFTER score_content;

ALTER TABLE ctx_rating
  ADD COLUMN coach_reply TEXT NULL AFTER content,
  ADD COLUMN appeal_status VARCHAR(16) NULL COMMENT 'NONE/PENDING/APPROVED/REJECTED' AFTER coach_reply;
