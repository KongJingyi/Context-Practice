/** 将 "02:45" / "1:05:20" 转为秒数 */
export function parseTimestamp(ts: string): number {
  const parts = ts.trim().split(":").map((p) => parseInt(p, 10));
  if (parts.some((n) => Number.isNaN(n))) return 0;
  if (parts.length === 2) return parts[0] * 60 + parts[1];
  if (parts.length === 3) return parts[0] * 3600 + parts[1] * 60 + parts[2];
  return 0;
}

export function formatSeconds(sec: number): string {
  const m = Math.floor(sec / 60);
  const s = Math.floor(sec % 60);
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

export function scoreEncouragement(score: number): string {
  if (score >= 90) return "卓越表现！您的表达已具备专业咨询级水准";
  if (score >= 80) return "表现优秀，继续保持结论先行的表达习惯";
  if (score >= 70) return "稳步提升中，重点优化论证结构与数据支撑";
  return "每一次练习都是进步，建议从开场 30 秒开始打磨";
}
