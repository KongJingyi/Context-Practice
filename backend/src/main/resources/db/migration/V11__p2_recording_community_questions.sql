-- P2: 云录制字段（幂等：若上次迁移在 INSERT 阶段失败，可安全重跑）
SET @db = DATABASE();
SELECT COUNT(*) INTO @recording_col_exists FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ctx_training_record' AND COLUMN_NAME = 'recording_url';
SET @ddl = IF(@recording_col_exists = 0,
  'ALTER TABLE ctx_training_record
    ADD COLUMN recording_url VARCHAR(512) NULL COMMENT ''回放地址 MP4/HLS'',
    ADD COLUMN recording_status VARCHAR(32) NULL DEFAULT ''IDLE'' COMMENT ''IDLE/RECORDING/PROCESSING/READY/FAILED'',
    ADD COLUMN trtc_task_id VARCHAR(128) NULL COMMENT ''TRTC 云录制任务 ID'',
    ADD COLUMN recording_expires_at DATETIME(3) NULL COMMENT ''回放过期时间''',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 社区种子（demo_user 发帖）
INSERT INTO ctx_post (author_id, kind, content, status, created_at, updated_at)
SELECT u.id, 'NOTE',
  '今天在对练中学会了如何处理「面试被打断」，关键在于心态：先停顿 0.5 秒，用一句话确认对方问题，再拉回主线。',
  1, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_post)
LIMIT 1;

INSERT INTO ctx_post (author_id, kind, content, status, created_at, updated_at)
SELECT u.id, 'HIGHLIGHT',
  '截取本次训练最精彩的一段表达，开门见山直接亮观点。',
  1, DATE_SUB(NOW(3), INTERVAL 5 HOUR), DATE_SUB(NOW(3), INTERVAL 5 HOUR)
FROM ctx_user u
WHERE u.username = 'demo_user'
  AND (SELECT COUNT(*) FROM ctx_post) < 2
LIMIT 1;

INSERT INTO ctx_post (author_id, kind, content, status, created_at, updated_at)
SELECT u.id, 'EXPERIENCE',
  '字节跳动 · 产品经理二面复盘\nQ1: 介绍一个失败项目及复盘\nQ2: 如何协调研发与设计的优先级',
  1, DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY)
FROM ctx_user u
WHERE u.username = 'demo_user'
  AND (SELECT COUNT(*) FROM ctx_post) < 3
LIMIT 1;

-- 题库种子（按场景）
INSERT INTO ctx_question_bank (scene_id, name, category, status, created_at, updated_at)
SELECT s.id, CONCAT(s.name, '题库'), LOWER(s.code), 1, NOW(3), NOW(3)
FROM ctx_scene s
WHERE s.status = 1
  AND NOT EXISTS (SELECT 1 FROM ctx_question_bank qb WHERE qb.scene_id = s.id);

INSERT INTO ctx_question (bank_id, title, difficulty, tags, status, created_at, updated_at)
SELECT qb.id, '请用 30 秒介绍你的核心优势', 2, '自我介绍', 1, NOW(3), NOW(3)
FROM ctx_question_bank qb
JOIN ctx_scene s ON s.id = qb.scene_id AND s.code = 'INTERVIEW'
WHERE NOT EXISTS (SELECT 1 FROM ctx_question q WHERE q.bank_id = qb.id);

INSERT INTO ctx_question (bank_id, title, difficulty, tags, status, created_at, updated_at)
SELECT qb.id, '项目延期时如何向上级汇报', 3, '汇报,STAR', 1, NOW(3), NOW(3)
FROM ctx_question_bank qb
JOIN ctx_scene s ON s.id = qb.scene_id AND s.code = 'REPORT'
WHERE NOT EXISTS (SELECT 1 FROM ctx_question q WHERE q.bank_id = qb.id AND q.title LIKE '%延期%');

INSERT INTO ctx_question (bank_id, title, difficulty, tags, status, created_at, updated_at)
SELECT qb.id, '遇到资源冲突如何协调', 3, '协调,沟通', 1, NOW(3), NOW(3)
FROM ctx_question_bank qb
JOIN ctx_scene s ON s.id = qb.scene_id AND s.code = 'COMMUNICATION'
WHERE NOT EXISTS (SELECT 1 FROM ctx_question q WHERE q.bank_id = qb.id AND q.title LIKE '%资源%');

INSERT INTO ctx_question (bank_id, title, difficulty, tags, status, created_at, updated_at)
SELECT qb.id, '如果数据与直觉冲突，你会如何决策？', 4, '压力,决策', 1, NOW(3), NOW(3)
FROM ctx_question_bank qb
JOIN ctx_scene s ON s.id = qb.scene_id AND s.code = 'PRESSURE'
WHERE NOT EXISTS (SELECT 1 FROM ctx_question q WHERE q.bank_id = qb.id AND q.title LIKE '%直觉%');
