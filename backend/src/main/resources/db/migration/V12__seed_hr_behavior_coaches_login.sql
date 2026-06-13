-- HR 行为面试场景四位陪练（与学员端列表一致）
-- 登录：手机号 + 验证码 888888（开发环境 SMS_PROVIDER=dev）
-- | 姓名   | 手机号        | username    |
-- | 林晚晴 | 13800001001   | coach_lin   |
-- | 周予安 | 13800001002   | coach_zhou  |
-- | 苏景行 | 13800001004   | coach_su    |
-- | 沈知夏 | 13800001005   | coach_shen  |

-- ========== 用户账号（幂等） ==========
INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_lin', '13800001001', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_lin');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_zhou', '13800001002', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_zhou');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_su', '13800001004', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_su');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_shen', '13800001005', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_shen');

-- ========== 陪练资料（插入或更新为截图信息） ==========
INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '周予安', '10年HRBP/面试官', cl.id, 1, 128.00, 'pressure,star', '压力面,八大问', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_zhou'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

UPDATE ctx_coach_profile cp
JOIN ctx_user u ON u.id = cp.user_id
JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
SET cp.nickname = '周予安',
    cp.bio = '10年HRBP/面试官',
    cp.level_id = cl.id,
    cp.status = 1,
    cp.price_per_30m = 128.00,
    cp.specialties = 'pressure,star',
    cp.service_tags = '压力面,八大问',
    cp.updated_at = NOW(3)
WHERE u.username = 'coach_zhou';

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '林晚晴', '前字节技术总监', cl.id, 1, 199.00, 'pressure,logic,star', '连环追问,STAR法则', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_lin'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

UPDATE ctx_coach_profile cp
JOIN ctx_user u ON u.id = cp.user_id
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
SET cp.nickname = '林晚晴',
    cp.bio = '前字节技术总监',
    cp.level_id = cl.id,
    cp.status = 1,
    cp.price_per_30m = 199.00,
    cp.specialties = 'pressure,logic,star',
    cp.service_tags = '连环追问,STAR法则',
    cp.updated_at = NOW(3)
WHERE u.username = 'coach_lin';

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '苏景行', '薪金金牌导师', cl.id, 1, 168.00, 'negotiate,logic', '薪酬谈判,商业逻辑', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_su'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

UPDATE ctx_coach_profile cp
JOIN ctx_user u ON u.id = cp.user_id
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
SET cp.nickname = '苏景行',
    cp.bio = '薪金金牌导师',
    cp.level_id = cl.id,
    cp.status = 1,
    cp.price_per_30m = 168.00,
    cp.specialties = 'negotiate,logic',
    cp.service_tags = '薪酬谈判,商业逻辑',
    cp.updated_at = NOW(3)
WHERE u.username = 'coach_su';

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '沈知夏', '500强HR总监', cl.id, 1, 158.00, 'star,pressure,social', '八大问,价值观引导', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_shen'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

UPDATE ctx_coach_profile cp
JOIN ctx_user u ON u.id = cp.user_id
JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
SET cp.nickname = '沈知夏',
    cp.bio = '500强HR总监',
    cp.level_id = cl.id,
    cp.status = 1,
    cp.price_per_30m = 158.00,
    cp.specialties = 'star,pressure,social',
    cp.service_tags = '八大问,价值观引导',
    cp.updated_at = NOW(3)
WHERE u.username = 'coach_shen';

-- ========== COACH + USER 角色（陪练端登录需 COACH） ==========
INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'COACH'
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- ========== 手机号登录凭证 ==========
INSERT INTO ctx_user_auth (user_id, provider, provider_uid, mobile, login_fail_count, created_at, updated_at)
SELECT u.id, 'MOBILE', u.mobile, u.mobile, 0, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_auth a WHERE a.user_id = u.id AND a.provider = 'MOBILE'
  );

-- ========== HR 行为面试场景关联 ==========
INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job-hr-behavior', 1, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job-hr-behavior'
  );

INSERT INTO ctx_coach_scene (coach_id, scene_key, sort_order, created_at, updated_at)
SELECT u.id, 'job', 2, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_coach_scene s WHERE s.coach_id = u.id AND s.scene_key = 'job'
  );

-- ========== 可预约时段（未来 14 天，若尚无） ==========
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
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_su', 'coach_shen')
AND NOT EXISTS (
    SELECT 1 FROM ctx_coach_schedule_slot s
    WHERE s.coach_id = u.id
      AND s.start_time = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val, 0, 0))
);
