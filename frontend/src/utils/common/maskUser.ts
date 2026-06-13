/** 用户名脱敏：张**三、w***9 */
export function maskUserDisplay(raw: string): string {
  const s = String(raw || "").trim();
  if (!s) return "***";
  if (s.length <= 1) return `${s}***`;
  if (s.length === 2) return `${s[0]}*${s[1]}`;
  if (/^[\u4e00-\u9fa5]+$/.test(s)) {
    return `${s[0]}**${s[s.length - 1]}`;
  }
  return `${s[0]}***${s[s.length - 1]}`;
}
