/** 18 位中国大陆居民身份证校验（含校验位） */
export function validateChineseIdCard(id: string): boolean {
  const s = id.trim().toUpperCase();
  if (!/^\d{17}[\dX]$/.test(s)) return false;
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
  const codes = "10X98765432";
  let sum = 0;
  for (let i = 0; i < 17; i += 1) {
    sum += Number(s[i]) * weights[i];
  }
  return codes[sum % 11] === s[17];
}
