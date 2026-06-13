-- 追加 3 条「训练中」订单（IN_SERVICE + IN_PROGRESS），便于陪练端多房间联调

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_zhao', '13800000021', 0, NOW(3), NOW(3)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_zhao');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_qian', '13800000022', 0, NOW(3), NOW(3)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_qian');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_sun', '13800000023', 0, NOW(3), NOW(3)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_sun');

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '赵六', '提升即兴演讲能力', NOW(3), NOW(3)
FROM ctx_user u WHERE u.username = 'demo_student_zhao'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '钱七', '强化技术面表达', NOW(3), NOW(3)
FROM ctx_user u WHERE u.username = 'demo_student_qian'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '孙八', '练习冲突场景沟通', NOW(3), NOW(3)
FROM ctx_user u WHERE u.username = 'demo_student_sun'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username IN ('demo_student_zhao', 'demo_student_qian', 'demo_student_sun')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- 订单：赵六 · 即兴演讲
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end, amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT u_stu.id, p.id, u_coach.id, s.id,
  DATE_SUB(NOW(3), INTERVAL 18 MINUTE),
  DATE_ADD(NOW(3), INTERVAL 42 MINUTE),
  199.00, 'IN_SERVICE', 0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_zhao'
JOIN ctx_scene s ON s.code = 'IMPROMPTU'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'd4e5f6a7b8c9012345678901234567');

-- 订单：钱七 · 面试模拟
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end, amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT u_stu.id, p.id, u_coach.id, s.id,
  DATE_SUB(NOW(3), INTERVAL 12 MINUTE),
  DATE_ADD(NOW(3), INTERVAL 48 MINUTE),
  199.00, 'IN_SERVICE', 0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_qian'
JOIN ctx_scene s ON s.code = 'INTERVIEW'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'e5f6a7b8c9d0123456789012345678');

-- 订单：孙八 · 冲突表达
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end, amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT u_stu.id, p.id, u_coach.id, s.id,
  DATE_SUB(NOW(3), INTERVAL 8 MINUTE),
  DATE_ADD(NOW(3), INTERVAL 52 MINUTE),
  199.00, 'IN_SERVICE', 0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 1 DAY), DATE_SUB(NOW(3), INTERVAL 1 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_sun'
JOIN ctx_scene s ON s.code = 'CONFLICT'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'f6a7b8c9d0e1234567890123456789');

INSERT INTO ctx_payment (order_id, channel, trade_no, amount, status, paid_at, created_at, updated_at)
SELECT o.id, 'MOCK', CONCAT('MOCK-SEED-', o.id), o.amount, 'SUCCESS', o.pay_at, NOW(3), NOW(3)
FROM ctx_order o
JOIN ctx_user u_stu ON o.user_id = u_stu.id
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
WHERE u_stu.username IN ('demo_student_zhao', 'demo_student_qian', 'demo_student_sun')
  AND o.status = 'IN_SERVICE'
  AND NOT EXISTS (SELECT 1 FROM ctx_payment p WHERE p.order_id = o.id);

INSERT INTO ctx_training_record (user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at)
SELECT o.user_id, o.id, 'd4e5f6a7b8c9012345678901234567', 'IN_PROGRESS',
  DATE_SUB(NOW(3), INTERVAL 15 MINUTE), NULL,
  DATE_SUB(NOW(3), INTERVAL 15 MINUTE), NOW(3)
FROM ctx_order o
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_student_zhao'
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
WHERE o.status = 'IN_SERVICE'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'd4e5f6a7b8c9012345678901234567');

INSERT INTO ctx_training_record (user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at)
SELECT o.user_id, o.id, 'e5f6a7b8c9d0123456789012345678', 'IN_PROGRESS',
  DATE_SUB(NOW(3), INTERVAL 10 MINUTE), NULL,
  DATE_SUB(NOW(3), INTERVAL 10 MINUTE), NOW(3)
FROM ctx_order o
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_student_qian'
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
WHERE o.status = 'IN_SERVICE'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'e5f6a7b8c9d0123456789012345678');

INSERT INTO ctx_training_record (user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at)
SELECT o.user_id, o.id, 'f6a7b8c9d0e1234567890123456789', 'IN_PROGRESS',
  DATE_SUB(NOW(3), INTERVAL 6 MINUTE), NULL,
  DATE_SUB(NOW(3), INTERVAL 6 MINUTE), NOW(3)
FROM ctx_order o
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_student_sun'
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
WHERE o.status = 'IN_SERVICE'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'f6a7b8c9d0e1234567890123456789');

INSERT INTO ctx_training_participant (training_id, user_id, role, joined_at, created_at, updated_at)
SELECT tr.id, tr.user_id, 'USER', tr.started_at, NOW(3), NOW(3)
FROM ctx_training_record tr
WHERE tr.room_id IN (
  'd4e5f6a7b8c9012345678901234567',
  'e5f6a7b8c9d0123456789012345678',
  'f6a7b8c9d0e1234567890123456789'
)
AND NOT EXISTS (
  SELECT 1 FROM ctx_training_participant tp
  WHERE tp.training_id = tr.id AND tp.user_id = tr.user_id AND tp.role = 'USER'
);

INSERT INTO ctx_training_participant (training_id, user_id, role, joined_at, created_at, updated_at)
SELECT tr.id, u_coach.id, 'COACH', tr.started_at, NOW(3), NOW(3)
FROM ctx_training_record tr
JOIN ctx_order o ON tr.order_id = o.id
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
WHERE tr.room_id IN (
  'd4e5f6a7b8c9012345678901234567',
  'e5f6a7b8c9d0123456789012345678',
  'f6a7b8c9d0e1234567890123456789'
)
AND NOT EXISTS (
  SELECT 1 FROM ctx_training_participant tp
  WHERE tp.training_id = tr.id AND tp.user_id = u_coach.id AND tp.role = 'COACH'
);
