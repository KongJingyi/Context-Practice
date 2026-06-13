/** 评论提交前简单敏感词过滤 */
const BLOCKED = ["违禁", "赌博", "色情", "辱骂"];

/**
 * @returns {{ ok: boolean; message?: string }}
 */
export function filterSensitiveText(text: string) {
  const t = text.trim();
  if (!t) return { ok: false, message: "请输入评论内容" };
  const hit = BLOCKED.find((w) => t.includes(w));
  if (hit) return { ok: false, message: "内容包含不当词汇，请修改后重试" };
  if (t.length > 500) return { ok: false, message: "评论过长，请控制在 500 字以内" };
  return { ok: true };
}
