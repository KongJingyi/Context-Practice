import fs from "node:fs";
import path from "node:path";
import zlib from "node:zlib";

function crc32(buf) {
  let c = ~0;
  for (let i = 0; i < buf.length; i++) {
    c ^= buf[i];
    for (let k = 0; k < 8; k++) c = (c >>> 1) ^ (0xedb88320 & -(c & 1));
  }
  return ~c >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const t = Buffer.from(type);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(Buffer.concat([t, data])));
  return Buffer.concat([len, t, data, crc]);
}

function makePng(w, h, paint) {
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) {
    raw[y * (w * 4 + 1)] = 0;
    for (let x = 0; x < w; x++) {
      const [r, g, b, a] = paint(x, y);
      const i = y * (w * 4 + 1) + 1 + x * 4;
      raw[i] = r;
      raw[i + 1] = g;
      raw[i + 2] = b;
      raw[i + 3] = a;
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk("IHDR", ihdr),
    chunk("IDAT", zlib.deflateSync(raw, { level: 9 })),
    chunk("IEND", Buffer.alloc(0)),
  ]);
}

function makeIco(pngPath, icoPath) {
  const png = fs.readFileSync(pngPath);
  const header = Buffer.alloc(6);
  header.writeUInt16LE(0, 0);
  header.writeUInt16LE(1, 2);
  header.writeUInt16LE(1, 4);
  const entry = Buffer.alloc(16);
  entry[0] = 32;
  entry[1] = 32;
  entry[4] = 1;
  entry[6] = 32;
  entry.writeUInt32LE(png.length, 8);
  entry.writeUInt32LE(22, 12);
  fs.writeFileSync(icoPath, Buffer.concat([header, entry, png]));
}

const blueSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" role="img" aria-label="语境智练">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#2563eb"/>
      <stop offset="100%" stop-color="#1d4ed8"/>
    </linearGradient>
  </defs>
  <rect width="32" height="32" rx="8" fill="url(#bg)"/>
  <path fill="#fff" d="M8 9.5h16a2.5 2.5 0 0 1 2.5 2.5v7.5A2.5 2.5 0 0 1 24 22h-9.8l-4.2 3.2V22H8a2.5 2.5 0 0 1-2.5-2.5V12A2.5 2.5 0 0 1 8 9.5z"/>
  <circle cx="12.5" cy="15.5" r="1.6" fill="#2563eb"/>
  <circle cx="16" cy="15.5" r="1.6" fill="#2563eb"/>
  <circle cx="19.5" cy="15.5" r="1.6" fill="#2563eb"/>
</svg>
`;

const coachSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" role="img" aria-label="Context 陪练">
  <defs>
    <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#0d9488"/>
      <stop offset="100%" stop-color="#14b8a6"/>
    </linearGradient>
  </defs>
  <rect width="32" height="32" rx="8" fill="url(#bg)"/>
  <text x="16" y="21" text-anchor="middle" fill="#fff" font-family="system-ui,sans-serif" font-size="11" font-weight="700">CP</text>
</svg>
`;

function bluePaint(x, y) {
  const corner = Math.max(Math.abs(x - 15.5), Math.abs(y - 15.5));
  const rounded = corner > 12 ? 0 : 255;
  const inRect = x >= 8 && x <= 24 && y >= 10 && y <= 22;
  const tail = x >= 6 && x <= 10 && y >= 20 && y <= 24 && x + y < 30;
  const bubble = inRect || tail;
  const bg = rounded ? [37, 99, 235, 255] : [0, 0, 0, 0];
  return bubble ? [255, 255, 255, 255] : bg;
}

function tealPaint(x, y) {
  const corner = Math.max(Math.abs(x - 15.5), Math.abs(y - 15.5));
  const rounded = corner > 12 ? 0 : 255;
  const bg = rounded ? [13, 148, 136, 255] : [0, 0, 0, 0];
  const inCp = x >= 10 && x <= 21 && y >= 12 && y <= 20;
  return inCp ? [255, 255, 255, 255] : bg;
}

const root = path.resolve(import.meta.dirname, "..");
const targets = [
  [path.join(root, "frontend/public"), blueSvg, bluePaint],
  [path.join(root, "frontend/static"), blueSvg, bluePaint],
  [path.join(root, "frontend/admin/public"), blueSvg, bluePaint],
  [path.join(root, "coach/public"), coachSvg, tealPaint],
];

for (const [dir, svgContent, paint] of targets) {
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, "favicon.svg"), svgContent, "utf8");
  const png = makePng(32, 32, paint);
  const pngPath = path.join(dir, "favicon.png");
  fs.writeFileSync(pngPath, png);
  makeIco(pngPath, path.join(dir, "favicon.ico"));
  console.log("generated", dir);
}
