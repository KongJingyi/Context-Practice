-- 陪练员样本、用户能力标签（供 GET /api/coaches、/api/user/ability-tags 等）

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_lin', '13800001001', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_lin');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_zhou', '13800001002', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_zhou');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'coach_chen', '13800001003', 0, NOW(3), NOW(3) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'coach_chen');

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '林晚晴', '前字节技术总监 · 结构化表达', cl.id, 1, 199.00, 'pressure,logic,star', '连环追问,STAR 法则', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_lin'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '周予安', '10 年 HRBP / 面试官', cl.id, 1, 128.00, 'pressure,star', '压力面,八大问', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'FORMAL'
WHERE u.username = 'coach_zhou'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, level_id, status, price_per_30m, specialties, service_tags, created_at, updated_at)
SELECT u.id, '陈默言', '麦肯锡沟通教练', cl.id, 1, 268.00, 'report,logic', '复盘答辩,数据叙事', NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_coach_level cl ON cl.code = 'GOLD'
WHERE u.username = 'coach_chen'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'COACH'
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_chen')
  AND NOT EXISTS (SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username IN ('coach_lin', 'coach_zhou', 'coach_chen')
  AND NOT EXISTS (SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

INSERT INTO ctx_user_ability_tag (user_id, tag, weight, created_at, updated_at)
SELECT u.id, t.tag, t.weight, NOW(3), NOW(3)
FROM ctx_user u
JOIN (
  SELECT '逻辑清晰' AS tag, 95 AS weight UNION ALL
  SELECT '表达流畅', 88 UNION ALL
  SELECT '抗压稳定', 82 UNION ALL
  SELECT '结构完整', 90 UNION ALL
  SELECT '数据支撑', 75
) t
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_ability_tag a WHERE a.user_id = u.id AND a.tag = t.tag);
