-- 演示账号：用户 demo_user、陪练 demo_coach（便于前后端联调）
-- 通过 POST /api/auth/login { "username": "demo_coach" } 获取 Token

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_user', '13800000001', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_user');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_coach', '13800000002', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_coach');

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '演示用户', '求职面试', NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_user'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_coach_profile (user_id, nickname, bio, status, price_per_30m, created_at, updated_at)
SELECT u.id, '李教练', '结构化表达陪练', 1, 99.00, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_coach'
  AND NOT EXISTS (SELECT 1 FROM ctx_coach_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username = 'demo_user'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'COACH'
WHERE u.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
