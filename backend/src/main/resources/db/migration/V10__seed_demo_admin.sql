-- 演示管理员账号 demo_admin（手机号 13800000003，验证码 888888）
INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_admin', '13800000003', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_admin');

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '系统管理员', NULL, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'ADMIN'
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO ctx_system_config (cfg_key, cfg_value, description, updated_by, created_at, updated_at)
SELECT 'platform.name', '语境智练', '平台名称', u.id, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (SELECT 1 FROM ctx_system_config c WHERE c.cfg_key = 'platform.name');

INSERT INTO ctx_system_config (cfg_key, cfg_value, description, updated_by, created_at, updated_at)
SELECT 'support.hotline', '400-000-0000', '客服热线', u.id, NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_admin'
  AND NOT EXISTS (SELECT 1 FROM ctx_system_config c WHERE c.cfg_key = 'support.hotline');
