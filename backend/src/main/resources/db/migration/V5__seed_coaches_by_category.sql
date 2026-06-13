-- 陪练-场景关联 + 按前端六大类补充陪练样本

CREATE TABLE IF NOT EXISTS ctx_coach_scene (
  coach_id BIGINT UNSIGNED NOT NULL,
  scene_key VARCHAR(64) NOT NULL COMMENT '前端 scene_id 或大类 id（job/mgmt/...）',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (coach_id, scene_key),
  KEY idx_ctx_coach_scene_key (scene_key, sort_order),
  CONSTRAINT fk_ctx_coach_scene_coach_id FOREIGN KEY (coach_id) REFERENCES ctx_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ========== 新陪练账号 ==========
INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_su', '13800001004', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_su');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_shen', '13800001005', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_shen');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_gu', '13800001006', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_gu');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_tang', '13800001007', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_tang');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_lu', '13800001008', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_lu');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_song', '13800001009', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_song');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_han', '13800001010', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_han');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_fang', '13800001011', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_fang');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_qin', '13800001012', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_qin');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_bai', '13800001013', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_bai');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_mo', '13800001014', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_mo');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_jiang', '13800001015', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_jiang');

-- ========== 陪练资料 ==========
INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '苏景行', '群面金牌导师 · 无领导讨论控场', cl.id, 1, 168.00, 'group,pressure,logic', '群面控场,观点提炼', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_su' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '沈知夏', '500强HR总监 · 行为面试引导', cl.id, 1, 158.00, 'star,pressure,social', '八大问,价值观引导', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_shen' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '顾清和', '上市公司培训负责人 · 汇报答辩', cl.id, 1, 228.00, 'report,logic', '结论先行,数据叙事', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_gu' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '唐意远', '财务BP · 预算与资源争取', cl.id, 1, 238.00, 'report,negotiate,logic', '预算争取,利益对齐', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_tang' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '陆星河', '即兴演讲教练 · 3分钟结构化输出', cl.id, 1, 148.00, 'speech,logic', '即兴演讲,观点收敛', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_lu' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '宋知微', '路演顾问 · 电梯演讲与闪击表达', cl.id, 1, 138.00, 'speech,social', '电梯演讲,卖点提炼', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_song' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '韩修然', '压力面教官 · 高强度连环追问', cl.id, 1, 178.00, 'pressure,logic,star', '连环追问,逻辑补位', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_han' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '方至诚', '竞技表达教练 · 倒计时限时应答', cl.id, 1, 128.00, 'pressure,speech,logic', '限时应答,极简表达', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_fang' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '秦语珊', '跨部门协调专家 · 资源对齐', cl.id, 1, 188.00, 'social,negotiate', '跨部门协调,共识收口', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_qin' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '白露晞', '组织发展顾问 · 向上反馈与建议', cl.id, 1, 168.00, 'social,report', '向上沟通,分寸感', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_bai' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '莫谨言', '职场边界教练 · 拒绝与尊严维护', cl.id, 1, 158.00, 'social,pressure', '委婉拒绝,边界感', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_mo' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '江屿舟', '冲突调解师 · 关键对话降级', cl.id, 1, 148.00, 'negotiate,social,pressure', '对话修复,情绪剥离', NOW(3), NOW(3)
FROM ctx_user u JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_jiang' AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

-- ========== COACH 角色 ==========
INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'COACH'
WHERE u.username IN (
  'coach_su','coach_shen','coach_gu','coach_tang','coach_lu','coach_song',
  'coach_han','coach_fang','coach_qin','coach_bai','coach_mo','coach_jiang'
)
AND NOT EXISTS (SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- ========== 场景关联（子场景 + 大类双绑，便于筛选） ==========
-- 职场求职 job
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-tech-deep', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_lin'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-tech-deep');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_lin'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-hr-behavior', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_zhou'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-hr-behavior');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_zhou'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-group-discuss', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_su'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-group-discuss');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_su'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-hr-behavior', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_shen'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-hr-behavior');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-tech-deep', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_shen'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-tech-deep');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_shen'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job');

-- 管理汇报 mgmt
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt-quarter-review', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_chen'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt-quarter-review');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_chen'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt-quarter-review', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_gu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt-quarter-review');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt-risk-report', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_gu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt-risk-report');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_gu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt-budget', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_tang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt-budget');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'mgmt', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_tang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'mgmt');

-- 即兴演讲 speech
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'speech-random-3min', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_lu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'speech-random-3min');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'speech-meeting-open', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_lu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'speech-meeting-open');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'speech', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_lu'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'speech');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'speech-elevator-30s', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_song'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'speech-elevator-30s');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'speech', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_song'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'speech');

-- 压力训练 pressure
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'pressure-chain-qa', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_han'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'pressure-chain-qa');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'pressure-logic-defense', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_han'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'pressure-logic-defense');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'pressure', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_han'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'pressure');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'pressure-countdown', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_fang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'pressure-countdown');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'pressure', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_fang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'pressure');

-- 职场沟通 communication
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'comm-cross-dept', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_qin'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'comm-cross-dept');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'comm-multi-party', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_qin'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'comm-multi-party');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'communication', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_qin'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'communication');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'comm-upward-feedback', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_bai'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'comm-upward-feedback');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'communication', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_bai'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'communication');

-- 冲突表达 conflict
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'conflict-say-no', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_mo'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'conflict-say-no');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'conflict-boundary', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_mo'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'conflict-boundary');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'conflict', 3, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_mo'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'conflict');

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'conflict-deescalate', 1, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_jiang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'conflict-deescalate');
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'conflict', 2, NOW(3), NOW(3) FROM ctx_user u WHERE u.username = 'coach_jiang'
AND NOT EXISTS (SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'conflict');

-- ========== 新陪练可预约时段（未来 14 天） ==========
INSERT INTO ctx_coach_schedule_slot (coach_id, start_time, end_time, status, created_at, updated_at)
SELECT u.id,
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val, 0, 0)),
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val + 1, 0, 0)),
       1, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_profile cp ON cp.user_id = u.id AND cp.status = 1
CROSS JOIN (
    SELECT 0 AS day_offset UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13
) d
CROSS JOIN (
    SELECT 9 AS hour_val UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 14 UNION ALL
    SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 19 UNION ALL SELECT 20
) h
WHERE u.username IN (
  'coach_su','coach_shen','coach_gu','coach_tang','coach_lu','coach_song',
  'coach_han','coach_fang','coach_qin','coach_bai','coach_mo','coach_jiang'
)
AND NOT EXISTS (
    SELECT 1 FROM ctx_coach_schedule_slot s
    WHERE s.coach_id = u.id
      AND s.start_time = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val, 0, 0))
);
