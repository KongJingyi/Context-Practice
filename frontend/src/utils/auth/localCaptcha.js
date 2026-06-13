const CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const CAPTCHA_PREFIX = "local_captcha_";

function randomCode(len = 4) {
  let s = "";
  for (let i = 0; i < len; i += 1) {
    s += CHARS[Math.floor(Math.random() * CHARS.length)];
  }
  return s;
}

function buildSvgDataUrl(code) {
  const lines = Array.from({ length: 3 }, (_, i) => {
    const x1 = 10 + i * 30;
    const y1 = 8 + (i % 2) * 20;
    const x2 = 100 - i * 15;
    const y2 = 36 - (i % 2) * 12;
    return `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="rgba(59,130,246,0.35)" stroke-width="1"/>`;
  }).join("");
  const chars = code
    .split("")
    .map((ch, i) => {
      const x = 18 + i * 22;
      const y = 28 + (i % 2 === 0 ? 2 : -2);
      const rotate = -12 + i * 8;
      return `<text x="${x}" y="${y}" fill="#1d4ed8" font-size="20" font-family="Arial,sans-serif" transform="rotate(${rotate} ${x} ${y})">${ch}</text>`;
    })
    .join("");
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="120" height="44" viewBox="0 0 120 44">
    <rect width="120" height="44" rx="8" fill="#e0f2fe"/>
    ${lines}
    ${chars}
  </svg>`;
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
}

/** 无后端时的本地图形验证码（仅开发演示） */
export function createLocalCaptcha() {
  const code = randomCode(4);
  const captchaKey = `${CAPTCHA_PREFIX}${Date.now()}`;
  uni.setStorageSync(captchaKey, code.toLowerCase());
  return {
    captchaKey,
    captchaImage: buildSvgDataUrl(code),
    _local: true,
  };
}

export function verifyLocalCaptcha(captchaKey, input) {
  if (!captchaKey || !String(captchaKey).startsWith(CAPTCHA_PREFIX)) {
    return false;
  }
  const expected = uni.getStorageSync(captchaKey);
  uni.removeStorageSync(captchaKey);
  return (
    Boolean(expected) &&
    String(input || "")
      .trim()
      .toLowerCase() === String(expected).toLowerCase()
  );
}

export function isLocalCaptchaKey(captchaKey) {
  return String(captchaKey || "").startsWith(CAPTCHA_PREFIX);
}
