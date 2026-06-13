-- 陪练端全流程联调：为 demo_coach 写入学员订单 / 训练 / 报告 / 评价
-- 覆盖 PAID（待训练）、IN_SERVICE（训练中）、COMPLETED（已完成）三种状态

-- =========================
-- 1. 演示学员账号
-- =========================

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_zhang', '13800000011', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_zhang');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_li', '13800000012', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_li');

INSERT INTO ctx_user (username, mobile, status, created_at, updated_at)
SELECT 'demo_student_wang', '13800000013', 0, NOW(3), NOW(3)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ctx_user WHERE username = 'demo_student_wang');

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '张三', '提升压力面试表达能力', NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_student_zhang'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '李四', '提升管理汇报逻辑性', NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_student_li'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_profile (user_id, nickname, training_goal, created_at, updated_at)
SELECT u.id, '王五', '提升客户谈判说服力', NOW(3), NOW(3)
FROM ctx_user u
WHERE u.username = 'demo_student_wang'
  AND NOT EXISTS (SELECT 1 FROM ctx_user_profile p WHERE p.user_id = u.id);

INSERT INTO ctx_user_role (user_id, role_id, created_at, updated_at)
SELECT u.id, r.id, NOW(3), NOW(3)
FROM ctx_user u
JOIN ctx_role r ON r.code = 'USER'
WHERE u.username IN ('demo_student_zhang', 'demo_student_li', 'demo_student_wang')
  AND NOT EXISTS (
    SELECT 1 FROM ctx_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- =========================
-- 2. 订单（相对当前时间，便于进入房间测试）
-- =========================

-- 2.1 待训练：张三 · 压力面试 · PAID（无 training，可点「进入房间」）
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end,
  amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT
  u_stu.id,
  p.id,
  u_coach.id,
  s.id,
  DATE_SUB(NOW(3), INTERVAL 3 MINUTE),
  DATE_ADD(NOW(3), INTERVAL 57 MINUTE),
  199.00,
  'PAID',
  0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 1 DAY),
  NOW(3), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_zhang'
JOIN ctx_scene s ON s.code = 'PRESSURE'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_order o
    WHERE o.coach_id = u_coach.id AND o.user_id = u_stu.id
      AND o.status = 'PAID' AND o.scene_id = s.id
      AND o.scheduled_start >= DATE_SUB(NOW(3), INTERVAL 2 HOUR)
  );

-- 2.2 训练中：李四 · 管理汇报 · IN_SERVICE + IN_PROGRESS
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end,
  amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT
  u_stu.id,
  p.id,
  u_coach.id,
  s.id,
  DATE_SUB(NOW(3), INTERVAL 25 MINUTE),
  DATE_ADD(NOW(3), INTERVAL 35 MINUTE),
  199.00,
  'IN_SERVICE',
  0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 2 DAY),
  DATE_SUB(NOW(3), INTERVAL 2 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_li'
JOIN ctx_scene s ON s.code = 'REPORT'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr
    WHERE tr.room_id = 'a1b2c3d4e5f6789012345678901234'
  );

-- 2.3 已完成（待提交反馈）：王五 · 客户谈判
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end,
  amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT
  u_stu.id,
  p.id,
  u_coach.id,
  s.id,
  DATE_SUB(NOW(3), INTERVAL 2 DAY),
  DATE_SUB(NOW(3), INTERVAL 2 DAY) + INTERVAL 60 MINUTE,
  199.00,
  'COMPLETED',
  0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 3 DAY),
  DATE_SUB(NOW(3), INTERVAL 3 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_student_wang'
JOIN ctx_scene s ON s.code = 'COMMUNICATION'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr
    WHERE tr.room_id = 'b2c3d4e5f6a7890123456789012345'
  );

-- 2.4 已完成（含报告+评价）：演示用户 demo_user · 面试模拟
INSERT INTO ctx_order (
  user_id, product_id, coach_id, scene_id,
  scheduled_start, scheduled_end,
  amount, status,
  platform_fee_rate, platform_fee_amount, coach_income_amount,
  pay_at, created_at, updated_at
)
SELECT
  u_stu.id,
  p.id,
  u_coach.id,
  s.id,
  DATE_SUB(NOW(3), INTERVAL 5 DAY),
  DATE_SUB(NOW(3), INTERVAL 5 DAY) + INTERVAL 60 MINUTE,
  199.00,
  'COMPLETED',
  0.1500, 29.85, 169.15,
  DATE_SUB(NOW(3), INTERVAL 6 DAY),
  DATE_SUB(NOW(3), INTERVAL 6 DAY), NOW(3)
FROM ctx_user u_coach
JOIN ctx_user u_stu ON u_stu.username = 'demo_user'
JOIN ctx_scene s ON s.code = 'INTERVIEW'
LEFT JOIN ctx_product p ON p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE'
WHERE u_coach.username = 'demo_coach'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr
    WHERE tr.room_id = 'c3d4e5f6a7b8901234567890123456'
  );

-- =========================
-- 3. 支付记录
-- =========================

INSERT INTO ctx_payment (order_id, channel, trade_no, amount, status, paid_at, created_at, updated_at)
SELECT o.id, 'MOCK', CONCAT('MOCK-SEED-', o.id), o.amount, 'SUCCESS', o.pay_at, NOW(3), NOW(3)
FROM ctx_order o
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
JOIN ctx_user u_stu ON o.user_id = u_stu.id
WHERE u_stu.username IN ('demo_student_zhang', 'demo_student_li', 'demo_student_wang', 'demo_user')
  AND o.status IN ('PAID', 'IN_SERVICE', 'COMPLETED')
  AND NOT EXISTS (SELECT 1 FROM ctx_payment p WHERE p.order_id = o.id);

-- =========================
-- 4. 训练会话 + 参与者
-- =========================

INSERT INTO ctx_training_record (
  user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at
)
SELECT
  o.user_id, o.id,
  'a1b2c3d4e5f6789012345678901234',
  'IN_PROGRESS',
  DATE_SUB(NOW(3), INTERVAL 20 MINUTE),
  NULL,
  DATE_SUB(NOW(3), INTERVAL 20 MINUTE), NOW(3)
FROM ctx_order o
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_student_li'
WHERE o.status = 'IN_SERVICE'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'a1b2c3d4e5f6789012345678901234'
  );

INSERT INTO ctx_training_record (
  user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at
)
SELECT
  o.user_id, o.id,
  'b2c3d4e5f6a7890123456789012345',
  'ENDED',
  DATE_SUB(NOW(3), INTERVAL 2 DAY),
  DATE_SUB(NOW(3), INTERVAL 2 DAY) + INTERVAL 55 MINUTE,
  DATE_SUB(NOW(3), INTERVAL 2 DAY), NOW(3)
FROM ctx_order o
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_student_wang'
WHERE o.status = 'COMPLETED'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'b2c3d4e5f6a7890123456789012345'
  );

INSERT INTO ctx_training_record (
  user_id, order_id, room_id, status, started_at, ended_at, created_at, updated_at
)
SELECT
  o.user_id, o.id,
  'c3d4e5f6a7b8901234567890123456',
  'REPORT_READY',
  DATE_SUB(NOW(3), INTERVAL 5 DAY),
  DATE_SUB(NOW(3), INTERVAL 5 DAY) + INTERVAL 58 MINUTE,
  DATE_SUB(NOW(3), INTERVAL 5 DAY), NOW(3)
FROM ctx_order o
JOIN ctx_user u_coach ON o.coach_id = u_coach.id AND u_coach.username = 'demo_coach'
JOIN ctx_user u_stu ON o.user_id = u_stu.id AND u_stu.username = 'demo_user'
WHERE o.status = 'COMPLETED'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_record tr WHERE tr.room_id = 'c3d4e5f6a7b8901234567890123456'
  );

INSERT INTO ctx_training_participant (training_id, user_id, role, joined_at, created_at, updated_at)
SELECT tr.id, tr.user_id, 'USER', tr.started_at, NOW(3), NOW(3)
FROM ctx_training_record tr
WHERE tr.room_id IN (
  'a1b2c3d4e5f6789012345678901234',
  'b2c3d4e5f6a7890123456789012345',
  'c3d4e5f6a7b8901234567890123456'
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
  'a1b2c3d4e5f6789012345678901234',
  'b2c3d4e5f6a7890123456789012345',
  'c3d4e5f6a7b8901234567890123456'
)
AND NOT EXISTS (
  SELECT 1 FROM ctx_training_participant tp
  WHERE tp.training_id = tr.id AND tp.user_id = u_coach.id AND tp.role = 'COACH'
);

-- =========================
-- 5. 训练报告（已完成订单）
-- =========================

INSERT INTO ctx_training_report (
  training_id, score_logic, score_fluency, score_pressure, score_content, score_time,
  highlights, coach_feedback, created_at, updated_at
)
SELECT
  tr.id, 4, 5, 4, 4, 5,
  JSON_ARRAY(
    JSON_OBJECT('label', '开场自我介绍', 'startSec', 30, 'endSec', 90),
    JSON_OBJECT('label', 'STAR 法则运用', 'startSec', 420, 'endSec', 540)
  ),
  '整体表达流畅，建议在压力追问环节再加强论据支撑。',
  NOW(3), NOW(3)
FROM ctx_training_record tr
WHERE tr.room_id = 'c3d4e5f6a7b8901234567890123456'
  AND NOT EXISTS (SELECT 1 FROM ctx_training_report r WHERE r.training_id = tr.id);

-- =========================
-- 6. 学员评价（收入/个人主页展示）
-- =========================

INSERT INTO ctx_rating (
  order_id, user_id, coach_id,
  score_professional, score_attitude, score_quality,
  content, tags, is_anonymous, coach_reply, appeal_status,
  status, created_at, updated_at
)
SELECT
  o.id, o.user_id, o.coach_id,
  5, 5, 4,
  '教练很专业，追问环节帮助很大，收获满满！',
  '专业,耐心,有针对性',
  0,
  '感谢认可，继续保持练习节奏！',
  'NONE',
  1,
  DATE_SUB(NOW(3), INTERVAL 4 DAY), NOW(3)
FROM ctx_order o
JOIN ctx_training_record tr ON tr.order_id = o.id AND tr.room_id = 'c3d4e5f6a7b8901234567890123456'
WHERE NOT EXISTS (SELECT 1 FROM ctx_rating r WHERE r.order_id = o.id);

-- =========================
-- 7. 训练笔记（陪练端高光标记）
-- =========================

INSERT INTO ctx_training_note (
  training_id, coach_id, note_type, label, content, start_sec, end_sec, created_at, updated_at
)
SELECT
  tr.id, o.coach_id, 'HIGHLIGHT', '逻辑清晰',
  '学员在汇报结论部分结构完整，先结论后论据。',
  180, 240, NOW(3), NOW(3)
FROM ctx_training_record tr
JOIN ctx_order o ON tr.order_id = o.id
WHERE tr.room_id = 'a1b2c3d4e5f6789012345678901234'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_note n
    WHERE n.training_id = tr.id AND n.label = '逻辑清晰'
  );

INSERT INTO ctx_training_note (
  training_id, coach_id, note_type, label, content, start_sec, end_sec, created_at, updated_at
)
SELECT
  tr.id, o.coach_id, 'ISSUE', '语速略快',
  '谈判开场语速偏快，建议放慢并增加停顿。',
  60, 120, NOW(3), NOW(3)
FROM ctx_training_record tr
JOIN ctx_order o ON tr.order_id = o.id
WHERE tr.room_id = 'b2c3d4e5f6a7890123456789012345'
  AND NOT EXISTS (
    SELECT 1 FROM ctx_training_note n
    WHERE n.training_id = tr.id AND n.label = '语速略快'
  );
