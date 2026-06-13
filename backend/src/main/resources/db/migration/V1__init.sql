-- MySQL 8.x
-- 语境智练：用户端 / 陪练员端 / 管理员端 统一库表结构（通过角色与关联表区分）。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 账号与权限
-- =========================

CREATE TABLE IF NOT EXISTS ctx_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  mobile VARCHAR(32) NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0 正常 1 冻结',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_user_username (username),
  KEY idx_ctx_user_mobile (mobile),
  KEY idx_ctx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_user_auth (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(32) NOT NULL COMMENT 'MOBILE / WECHAT / QQ',
  provider_uid VARCHAR(128) NULL COMMENT '第三方平台唯一标识',
  mobile VARCHAR(32) NULL,
  password_hash VARCHAR(255) NULL,
  last_login_at DATETIME(3) NULL,
  login_fail_count INT NOT NULL DEFAULT 0,
  locked_until DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_user_auth_user_provider (user_id, provider),
  UNIQUE KEY uk_ctx_user_auth_provider_uid (provider, provider_uid),
  KEY idx_ctx_user_auth_mobile (mobile),
  CONSTRAINT fk_ctx_user_auth_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL COMMENT 'USER / COACH / ADMIN',
  name VARCHAR(64) NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_user_role (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_user_role_user_role (user_id, role_id),
  KEY idx_ctx_user_role_role_id (role_id),
  CONSTRAINT fk_ctx_user_role_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_user_role_role_id FOREIGN KEY (role_id) REFERENCES ctx_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_account_restriction (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  restriction_code VARCHAR(32) NOT NULL COMMENT 'NO_POST / NO_ORDER / NO_MESSAGE',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 生效 0 解除',
  reason VARCHAR(255) NULL,
  start_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  end_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_account_restriction_user (user_id, restriction_code, status),
  CONSTRAINT fk_ctx_account_restriction_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 用户端：个人中心 / 认证 / 成长档案
-- =========================

CREATE TABLE IF NOT EXISTS ctx_user_profile (
  user_id BIGINT UNSIGNED NOT NULL,
  avatar_url VARCHAR(512) NULL,
  nickname VARCHAR(64) NULL,
  training_goal VARCHAR(64) NULL COMMENT '求职面试/晋升汇报/公众演讲等',
  real_name VARCHAR(64) NULL,
  identity_type VARCHAR(32) NULL COMMENT 'STUDENT / WORKER / OTHER',
  organization VARCHAR(128) NULL COMMENT '单位/学校',
  contact_phone VARCHAR(32) NULL,
  verified_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 未认证 1 审核中 2 已认证 3 驳回',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id),
  CONSTRAINT fk_ctx_user_profile_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_verification_request (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  kind VARCHAR(32) NOT NULL COMMENT 'USER_BASIC / USER_ID_DOC / COACH_REALNAME / COACH_QUALIFICATION',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0 待审核 1 通过 2 驳回',
  submit_payload JSON NULL,
  reviewer_id BIGINT UNSIGNED NULL,
  reviewed_at DATETIME(3) NULL,
  review_note VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_verification_user (user_id, kind, status),
  KEY idx_ctx_verification_status (status, created_at),
  CONSTRAINT fk_ctx_verification_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_verification_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_user_ability_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  tag VARCHAR(64) NOT NULL,
  weight INT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_user_ability_tag (user_id, tag),
  CONSTRAINT fk_ctx_user_ability_tag_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 场景 / 题库（用户端 + 陪练员端 + 管理员端）
-- =========================

CREATE TABLE IF NOT EXISTS ctx_scene (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(512) NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_scene_code (code),
  KEY idx_ctx_scene_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_question_bank (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  scene_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(128) NOT NULL,
  category VARCHAR(64) NULL COMMENT '行业/岗位/类型等',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_question_bank_scene (scene_id, status),
  CONSTRAINT fk_ctx_question_bank_scene_id FOREIGN KEY (scene_id) REFERENCES ctx_scene(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  bank_id BIGINT UNSIGNED NOT NULL,
  title VARCHAR(512) NOT NULL,
  difficulty TINYINT NULL COMMENT '1-5',
  tags VARCHAR(255) NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_question_bank (bank_id, status),
  CONSTRAINT fk_ctx_question_bank_id FOREIGN KEY (bank_id) REFERENCES ctx_question_bank(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_favorite (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  target_type VARCHAR(32) NOT NULL COMMENT 'COACH / QUESTION / POST',
  target_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_favorite (user_id, target_type, target_id),
  KEY idx_ctx_favorite_target (target_type, target_id),
  CONSTRAINT fk_ctx_favorite_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 陪练员端：资料/等级/排班
-- =========================

CREATE TABLE IF NOT EXISTS ctx_coach_level (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL COMMENT 'INTERN / FORMAL / GOLD',
  name VARCHAR(64) NOT NULL,
  min_completed_orders INT NOT NULL DEFAULT 0,
  min_rating DECIMAL(3,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_coach_level_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_coach_profile (
  user_id BIGINT UNSIGNED NOT NULL,
  avatar_url VARCHAR(512) NULL,
  nickname VARCHAR(64) NULL,
  bio VARCHAR(512) NULL,
  level_id BIGINT UNSIGNED NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 可接单 0 暂停/封禁',
  price_per_30m DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  specialties VARCHAR(255) NULL COMMENT '擅长场景/标签',
  service_tags VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (user_id),
  KEY idx_ctx_coach_profile_level (level_id, status),
  CONSTRAINT fk_ctx_coach_profile_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_coach_profile_level_id FOREIGN KEY (level_id) REFERENCES ctx_coach_level(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_coach_certificate (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  coach_id BIGINT UNSIGNED NOT NULL,
  kind VARCHAR(64) NOT NULL COMMENT '学信网/获奖证书/服务承诺书等',
  file_url VARCHAR(512) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0 待审 1 通过 2 驳回',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_coach_certificate (coach_id, status),
  CONSTRAINT fk_ctx_coach_certificate_coach_id FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_coach_schedule_slot (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  coach_id BIGINT UNSIGNED NOT NULL,
  start_time DATETIME(3) NOT NULL,
  end_time DATETIME(3) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 可预约 0 已占用/关闭',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_coach_slot (coach_id, start_time, end_time),
  KEY idx_ctx_coach_slot_time (coach_id, start_time, status),
  CONSTRAINT fk_ctx_coach_schedule_slot_coach_id FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 订单与交易
-- =========================

CREATE TABLE IF NOT EXISTS ctx_product (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  scene_id BIGINT UNSIGNED NULL,
  name VARCHAR(128) NOT NULL,
  kind VARCHAR(32) NOT NULL COMMENT 'SINGLE / PACKAGE / COUPON',
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  duration_minutes INT NULL COMMENT '单次/套餐的单节时长',
  sessions_count INT NULL COMMENT '套餐包含节数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 上架 0 下架',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_product_scene (scene_id, status),
  CONSTRAINT fk_ctx_product_scene_id FOREIGN KEY (scene_id) REFERENCES ctx_scene(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_order (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NULL,
  coach_id BIGINT UNSIGNED NULL,
  scene_id BIGINT UNSIGNED NULL,
  scheduled_start DATETIME(3) NULL,
  scheduled_end DATETIME(3) NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(32) NOT NULL COMMENT 'PENDING_PAY/PAID/CANCELLED/IN_SERVICE/COMPLETED/REFUNDING/REFUNDED',
  platform_fee_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
  platform_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  coach_income_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  pay_at DATETIME(3) NULL,
  cancel_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_order_user (user_id, created_at),
  KEY idx_ctx_order_coach (coach_id, scheduled_start),
  KEY idx_ctx_order_status (status, created_at),
  CONSTRAINT fk_ctx_order_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_order_product_id FOREIGN KEY (product_id) REFERENCES ctx_product(id),
  CONSTRAINT fk_ctx_order_coach_id FOREIGN KEY (coach_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_order_scene_id FOREIGN KEY (scene_id) REFERENCES ctx_scene(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_payment (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  channel VARCHAR(32) NOT NULL COMMENT 'WECHAT / ALIPAY / COUPON / MOCK',
  trade_no VARCHAR(128) NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(32) NOT NULL COMMENT 'INIT/SUCCESS/FAILED/CLOSED',
  paid_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_payment_order (order_id),
  KEY idx_ctx_payment_trade (trade_no),
  CONSTRAINT fk_ctx_payment_order_id FOREIGN KEY (order_id) REFERENCES ctx_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_refund (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  reason VARCHAR(255) NULL,
  status VARCHAR(32) NOT NULL COMMENT 'APPLIED/APPROVED/REJECTED/REFUNDED',
  decided_by BIGINT UNSIGNED NULL,
  decided_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_refund_order (order_id, status),
  CONSTRAINT fk_ctx_refund_order_id FOREIGN KEY (order_id) REFERENCES ctx_order(id),
  CONSTRAINT fk_ctx_refund_decided_by FOREIGN KEY (decided_by) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 实时训练会话 / 报告 / 评价
-- =========================

CREATE TABLE IF NOT EXISTS ctx_training_record (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  order_id BIGINT UNSIGNED NULL,
  room_id VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL COMMENT 'IN_PROGRESS/ENDED/REPORT_READY',
  started_at DATETIME(3) NULL,
  ended_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_training_room (room_id),
  KEY idx_ctx_training_user (user_id, created_at),
  KEY idx_ctx_training_order (order_id),
  CONSTRAINT fk_ctx_training_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_training_order_id FOREIGN KEY (order_id) REFERENCES ctx_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_training_participant (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  training_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  role VARCHAR(16) NOT NULL COMMENT 'USER/COACH',
  joined_at DATETIME(3) NULL,
  left_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_training_participant (training_id, user_id, role),
  KEY idx_ctx_training_participant_user (user_id),
  CONSTRAINT fk_ctx_training_participant_training_id FOREIGN KEY (training_id) REFERENCES ctx_training_record(id),
  CONSTRAINT fk_ctx_training_participant_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_training_report (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  training_id BIGINT UNSIGNED NOT NULL,
  pdf_url VARCHAR(512) NULL,
  score_logic TINYINT NULL COMMENT '1-5',
  score_fluency TINYINT NULL COMMENT '1-5',
  score_pressure TINYINT NULL COMMENT '1-5',
  highlights JSON NULL COMMENT '高光片段（含下载/分享链接等）',
  coach_feedback TEXT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_training_report_training (training_id),
  CONSTRAINT fk_ctx_training_report_training_id FOREIGN KEY (training_id) REFERENCES ctx_training_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_rating (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  coach_id BIGINT UNSIGNED NOT NULL,
  score_professional TINYINT NOT NULL,
  score_attitude TINYINT NOT NULL,
  score_quality TINYINT NOT NULL,
  content VARCHAR(1024) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 正常 0 屏蔽/删除',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_rating_order (order_id),
  KEY idx_ctx_rating_coach (coach_id, created_at),
  KEY idx_ctx_rating_user (user_id, created_at),
  CONSTRAINT fk_ctx_rating_order_id FOREIGN KEY (order_id) REFERENCES ctx_order(id),
  CONSTRAINT fk_ctx_rating_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_rating_coach_id FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 社区与互动：动态/评论/点赞/私信
-- =========================

CREATE TABLE IF NOT EXISTS ctx_post (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  author_id BIGINT UNSIGNED NOT NULL,
  kind VARCHAR(32) NOT NULL COMMENT 'NOTE/HIGHLIGHT/EXPERIENCE',
  content TEXT NOT NULL,
  media_urls JSON NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 正常 0 删除/隐藏',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_post_author (author_id, created_at),
  KEY idx_ctx_post_status (status, created_at),
  CONSTRAINT fk_ctx_post_author_id FOREIGN KEY (author_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_post_comment (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  author_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NULL,
  content VARCHAR(1024) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_post_comment_post (post_id, created_at),
  KEY idx_ctx_post_comment_parent (parent_id),
  CONSTRAINT fk_ctx_post_comment_post_id FOREIGN KEY (post_id) REFERENCES ctx_post(id),
  CONSTRAINT fk_ctx_post_comment_author_id FOREIGN KEY (author_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_post_comment_parent_id FOREIGN KEY (parent_id) REFERENCES ctx_post_comment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_post_like (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_post_like (post_id, user_id),
  KEY idx_ctx_post_like_user (user_id),
  CONSTRAINT fk_ctx_post_like_post_id FOREIGN KEY (post_id) REFERENCES ctx_post(id),
  CONSTRAINT fk_ctx_post_like_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_message (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  sender_id BIGINT UNSIGNED NOT NULL,
  receiver_id BIGINT UNSIGNED NOT NULL,
  kind VARCHAR(32) NOT NULL COMMENT 'TEXT/IMAGE/REPORT_SNIPPET',
  content TEXT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1 正常 0 删除',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_message_pair (sender_id, receiver_id, created_at),
  KEY idx_ctx_message_receiver (receiver_id, created_at),
  CONSTRAINT fk_ctx_message_sender_id FOREIGN KEY (sender_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_message_receiver_id FOREIGN KEY (receiver_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 投诉工单
-- =========================

CREATE TABLE IF NOT EXISTS ctx_complaint (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NULL,
  complainant_id BIGINT UNSIGNED NOT NULL,
  target_user_id BIGINT UNSIGNED NULL,
  kind VARCHAR(64) NOT NULL COMMENT '态度恶劣/未按时上线/违规言论等',
  content VARCHAR(1024) NOT NULL,
  evidence_urls JSON NULL,
  status VARCHAR(32) NOT NULL COMMENT 'SUBMITTED/IN_REVIEW/RESOLVED/REJECTED',
  handled_by BIGINT UNSIGNED NULL,
  handled_at DATETIME(3) NULL,
  result_note VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_complaint_status (status, created_at),
  KEY idx_ctx_complaint_complainant (complainant_id, created_at),
  CONSTRAINT fk_ctx_complaint_order_id FOREIGN KEY (order_id) REFERENCES ctx_order(id),
  CONSTRAINT fk_ctx_complaint_complainant_id FOREIGN KEY (complainant_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_complaint_target_user_id FOREIGN KEY (target_user_id) REFERENCES ctx_user(id),
  CONSTRAINT fk_ctx_complaint_handled_by FOREIGN KEY (handled_by) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 管理端：系统配置 / 公告培训 / 审计日志 / 备份信息（占位）
-- =========================

CREATE TABLE IF NOT EXISTS ctx_announcement (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  content TEXT NOT NULL,
  kind VARCHAR(32) NOT NULL COMMENT 'TRAINING/NORM/NOTICE',
  status TINYINT NOT NULL DEFAULT 1,
  published_at DATETIME(3) NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_announcement_status (status, published_at),
  CONSTRAINT fk_ctx_announcement_created_by FOREIGN KEY (created_by) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_system_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cfg_key VARCHAR(128) NOT NULL,
  cfg_value TEXT NOT NULL,
  description VARCHAR(255) NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_system_config_key (cfg_key),
  CONSTRAINT fk_ctx_system_config_updated_by FOREIGN KEY (updated_by) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_audit_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_id BIGINT UNSIGNED NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(32) NULL,
  target_id BIGINT UNSIGNED NULL,
  detail JSON NULL,
  ip VARCHAR(64) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_audit_actor (actor_id, created_at),
  KEY idx_ctx_audit_action (action, created_at),
  CONSTRAINT fk_ctx_audit_actor_id FOREIGN KEY (actor_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 预置数据：角色、场景、陪练等级（基础种子）
-- =========================

INSERT INTO ctx_role (code, name) VALUES
  ('USER', '用户'),
  ('COACH', '陪练员'),
  ('ADMIN', '管理员')
ON DUPLICATE KEY UPDATE name=VALUES(name);

INSERT INTO ctx_scene (code, name, description) VALUES
  ('INTERVIEW', '面试模拟', '技术面/HR面/压力面'),
  ('REPORT', '工作汇报', '项目答辩/周报/复盘'),
  ('IMPROMPTU', '即兴演讲', '随机抽题限时输出'),
  ('PRESSURE', '压力训练', '追问+时间压迫'),
  ('COMMUNICATION', '职场沟通', '跨部门协调/资源争取'),
  ('CONFLICT', '冲突表达', '向上反馈/拒绝不合理请求')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

INSERT INTO ctx_coach_level (code, name, min_completed_orders, min_rating) VALUES
  ('INTERN', '实习陪练', 0, 0.00),
  ('FORMAL', '正式陪练', 30, 4.50),
  ('GOLD', '金牌陪练', 200, 4.70)
ON DUPLICATE KEY UPDATE name=VALUES(name), min_completed_orders=VALUES(min_completed_orders), min_rating=VALUES(min_rating);

SET FOREIGN_KEY_CHECKS = 1;

