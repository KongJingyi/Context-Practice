-- 评价扩展：标签与匿名

ALTER TABLE ctx_rating
  ADD COLUMN tags VARCHAR(512) NULL COMMENT '评价标签，逗号分隔' AFTER content,
  ADD COLUMN is_anonymous TINYINT NOT NULL DEFAULT 0 COMMENT '1 匿名展示' AFTER tags;
