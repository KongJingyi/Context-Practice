-- 实战圈社区 + 练习实验室

SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'ctx_post' AND COLUMN_NAME = 'coach_id') = 0,
  'ALTER TABLE ctx_post
     ADD COLUMN coach_id BIGINT UNSIGNED NULL COMMENT ''关联陪练员 user_id'' AFTER author_id,
     ADD COLUMN title VARCHAR(256) NULL AFTER kind,
     ADD COLUMN hot_score INT NOT NULL DEFAULT 0 COMMENT ''热门排序权重'' AFTER content,
     ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER hot_score,
     ADD COLUMN comment_count INT NOT NULL DEFAULT 0 AFTER like_count,
     ADD COLUMN collect_count INT NOT NULL DEFAULT 0 AFTER comment_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS ctx_post_collect (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ctx_post_collect (post_id, user_id),
  KEY idx_ctx_post_collect_user (user_id),
  CONSTRAINT fk_ctx_post_collect_post_id FOREIGN KEY (post_id) REFERENCES ctx_post(id),
  CONSTRAINT fk_ctx_post_collect_user_id FOREIGN KEY (user_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_practice_category (
  id VARCHAR(32) NOT NULL,
  title VARCHAR(64) NOT NULL,
  subtitle VARCHAR(128) NULL,
  icon VARCHAR(16) NULL,
  gradient_start VARCHAR(16) NULL,
  gradient_end VARCHAR(16) NULL,
  today_count INT NOT NULL DEFAULT 0,
  span VARCHAR(16) NOT NULL DEFAULT 'normal',
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ctx_practice_question (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_id VARCHAR(32) NOT NULL,
  text VARCHAR(512) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ctx_practice_question_cat (category_id, sort_order),
  CONSTRAINT fk_ctx_practice_question_cat FOREIGN KEY (category_id) REFERENCES ctx_practice_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 练习实验室分类
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'interview', '面试求职', '自我介绍 · 离职原因 · 薪资谈判', '🎯', '#064e3b', '#10b981', 238, 'wide', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'interview');
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'report', '职场汇报', '周报 · 项目复盘 · 向上管理', '📊', '#1e3a8a', '#3b82f6', 186, 'normal', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'report');
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'negotiation', '商务谈判', '议价 · 合同条款 · 资源争取', '🤝', '#312e81', '#6366f1', 142, 'tall', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'negotiation');
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'speech', '公众演讲', '开场白 · 观点阐述 · 收尾', '🎤', '#134e4a', '#14b8a6', 167, 'normal', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'speech');
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'social', '日常社交', '破冰 · 寒暄 · 饭局应对', '💬', '#0c4a6e', '#0ea5e9', 203, 'normal', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'social');
INSERT INTO ctx_practice_category (id, title, subtitle, icon, gradient_start, gradient_end, today_count, span, sort_order)
SELECT 'objection', '异议处理', '拒绝加班 · 应对质疑 · 冲突化解', '⚡', '#4c1d95', '#8b5cf6', 195, 'wide', 6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_category WHERE id = 'objection');

INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'interview', '请做一段 3 分钟的自我介绍', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'interview' AND text LIKE '请做一段%');
INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'interview', '为什么要离开上一家公司？', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'interview' AND text LIKE '为什么要离开%');
INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'interview', '你的核心竞争力是什么？', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'interview' AND text LIKE '你的核心竞争力%');
INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'report', '向领导汇报本季度项目进展（2 分钟）', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'report' AND text LIKE '向领导汇报%');
INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'negotiation', '客户要求降价 15%，你如何回应？', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'negotiation' AND text LIKE '客户要求降价%');
INSERT INTO ctx_practice_question (category_id, text, sort_order)
SELECT 'speech', '部门年会 5 分钟开场致辞', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_practice_question WHERE category_id = 'speech' AND text LIKE '部门年会%');

-- 陪练灵感（author = coach）
INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT c.user_id, c.user_id, 'NOTE', '面试被打断时如何稳住节奏',
       '先复述对方问题争取 3 秒思考，再用「核心结论是…」拉回主线。陪练中多数学员第二次明显更稳。',
       JSON_OBJECT('type','insight','tags', JSON_ARRAY('#面试技巧','#心态建设'), 'display_medal','麦肯锡教练'),
       320, 128, 45, 32, 1, DATE_SUB(NOW(3), INTERVAL 2 HOUR), NOW(3)
FROM ctx_coach_profile c
WHERE c.nickname = '陈默言'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '面试被打断时如何稳住节奏')
LIMIT 1;

INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT c.user_id, c.user_id, 'HIGHLIGHT', '15s 高光：结论先行的开场',
       '开门见山亮观点，再展开论据。适合汇报与答辩场景。',
       JSON_OBJECT('type','highlight','tags', JSON_ARRAY('#高光片段','#公众演讲'),
         'video_preview','https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
         'display_medal','表达教练'),
       410, 256, 18, 89, 1, DATE_SUB(NOW(3), INTERVAL 5 HOUR), NOW(3)
FROM ctx_coach_profile c
WHERE c.nickname = '林晚晴'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '15s 高光：结论先行的开场')
LIMIT 1;

INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT c.user_id, c.user_id, 'NOTE', '汇报答辩：四步结构不出错',
       '结论 → 数据 → 风险 → 下一步。学员练三次后基本能在 2 分钟内讲清 ROI。',
       JSON_OBJECT('type','insight','tags', JSON_ARRAY('#职场汇报'), 'display_medal','HRBP'),
       180, 89, 12, 44, 1, DATE_SUB(NOW(3), INTERVAL 2 DAY), NOW(3)
FROM ctx_coach_profile c
WHERE c.nickname = '周予安'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '汇报答辩：四步结构不出错')
LIMIT 1;

-- 学员面经展示（author 用 demo_user，展示名写在 JSON，coach_id 关联陪练）
INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT u.id, c.user_id, 'EXPERIENCE', '字节跳动 · 产品经理二面复盘',
       'Q1: 介绍一个失败项目及复盘\nQ2: 如何协调研发与设计的优先级\nQ3: 如果数据与直觉冲突怎么办',
       JSON_OBJECT('type','interview','tags', JSON_ARRAY('#字节跳动','#产品经理','#面经'),
         'company','字节跳动','role','产品经理',
         'display_name','陈航','display_medal','面经达人','display_as_student', true),
       520, 412, 96, 210, 1, DATE_SUB(NOW(3), INTERVAL 1 DAY), NOW(3)
FROM ctx_user u
JOIN ctx_coach_profile c ON c.nickname = '陈默言'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '字节跳动 · 产品经理二面复盘')
LIMIT 1;

INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT u.id, c.user_id, 'EXPERIENCE', '腾讯 · 技术总监终面经验',
       '终面更看重技术决策的逻辑链而非细节背诵。STAR 里 S 不要超过 30 秒。',
       JSON_OBJECT('type','interview','tags', JSON_ARRAY('#腾讯','#技术管理'),
         'company','腾讯','role','技术总监',
         'display_name','苏景行','display_medal','逻辑大师','display_as_student', true),
       380, 302, 58, 145, 1, DATE_SUB(NOW(3), INTERVAL 4 DAY), NOW(3)
FROM ctx_user u
JOIN ctx_coach_profile c ON c.nickname = '林晚晴'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '腾讯 · 技术总监终面经验')
LIMIT 1;

INSERT INTO ctx_post (author_id, coach_id, kind, title, content, media_urls, hot_score, like_count, comment_count, collect_count, status, created_at, updated_at)
SELECT u.id, c.user_id, 'EXPERIENCE', '我是如何克服面试紧张的',
       '对练中学会了如何处理「面试被打断」，关键是先停顿 0.5 秒，用一句话确认对方问题，再拉回主线。',
       JSON_OBJECT('type','insight','tags', JSON_ARRAY('#面试技巧','#心态建设'),
         'display_name','张泽华','display_medal','逻辑大师','display_as_student', true),
       290, 128, 45, 32, 1, DATE_SUB(NOW(3), INTERVAL 3 HOUR), NOW(3)
FROM ctx_user u
JOIN ctx_coach_profile c ON c.nickname = '周予安'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_post p WHERE p.title = '我是如何克服面试紧张的')
LIMIT 1;
