-- 预约联调：商品、陪练可预约时段（未来 14 天，每日 9:00–18:00）

INSERT INTO ctx_product (scene_id, name, kind, price, duration_minutes, sessions_count, status, created_at, updated_at)
SELECT s.id, '单次陪练（60分钟）', 'SINGLE', 199.00, 60, 1, 1, NOW(3), NOW(3)
FROM ctx_scene s
WHERE s.code = 'INTERVIEW'
  AND NOT EXISTS (SELECT 1 FROM ctx_product p WHERE p.name = '单次陪练（60分钟）' AND p.kind = 'SINGLE');

INSERT INTO ctx_coach_schedule_slot (coach_id, start_time, end_time, status, created_at, updated_at)
SELECT u.id,
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val, 0, 0)),
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val + 1, 0, 0)),
       1,
       NOW(3),
       NOW(3)
FROM ctx_user u
JOIN ctx_coach_profile cp ON cp.user_id = u.id AND cp.status = 1
CROSS JOIN (
    SELECT 0 AS day_offset UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL
    SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13
) d
CROSS JOIN (
    SELECT 9 AS hour_val UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL
    SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17
) h
WHERE NOT EXISTS (
    SELECT 1 FROM ctx_coach_schedule_slot s
    WHERE s.coach_id = u.id
      AND s.start_time = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL d.day_offset DAY), MAKETIME(h.hour_val, 0, 0))
);
