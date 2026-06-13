/**
 * 解码路由 query（兼容 H5 二次编码：%25E5%25A4%25A7 → 中文）
 * @param {string | undefined} value
 * @returns {string}
 */
export function decodeQueryParam(value) {
  if (value == null || value === "") return "";
  let s = String(value).replace(/\+/g, " ");
  for (let i = 0; i < 3; i += 1) {
    if (!/%[0-9A-Fa-f]{2}/.test(s)) break;
    try {
      const next = decodeURIComponent(s);
      if (next === s) break;
      s = next;
    } catch {
      break;
    }
  }
  return s;
}
