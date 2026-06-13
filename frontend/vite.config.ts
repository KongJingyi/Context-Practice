import path from "node:path";
import fs from "node:fs";
import { defineConfig, loadEnv, type Plugin } from "vite";
import uni from "@dcloudio/vite-plugin-uni";
import basicSsl from "@vitejs/plugin-basic-ssl";

const FAVICON_FILES = ["favicon.ico", "favicon.png", "favicon.svg"] as const;
const FAVICON_MIME: Record<string, string> = {
  ".ico": "image/x-icon",
  ".png": "image/png",
  ".svg": "image/svg+xml",
};

/** uni-app 的 static 目录挂在 /static；浏览器默认请求根路径 /favicon.ico */
function faviconAtRootPlugin(staticDir: string): Plugin {
  return {
    name: "favicon-at-root",
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = req.url?.split("?")[0] ?? "";
        const name = FAVICON_FILES.find((file) => url === `/${file}`);
        if (!name) return next();

        const filePath = path.join(staticDir, name);
        if (!fs.existsSync(filePath)) return next();

        res.statusCode = 200;
        res.setHeader("Content-Type", FAVICON_MIME[path.extname(name)] ?? "application/octet-stream");
        res.setHeader("Cache-Control", "no-cache");
        fs.createReadStream(filePath).pipe(res);
      });
    },
    closeBundle() {
      const outDirs = [
        path.resolve(process.cwd(), "dist/build/h5"),
        path.resolve(process.cwd(), "dist/dev/h5"),
      ];
      for (const outDir of outDirs) {
        if (!fs.existsSync(outDir)) continue;
        for (const name of FAVICON_FILES) {
          const src = path.join(staticDir, name);
          if (!fs.existsSync(src)) continue;
          fs.copyFileSync(src, path.join(outDir, name));
        }
      }
    },
  };
}

/**
 * Uni-app + Vite：同一套代码发布 H5 与微信小程序。
 * - H5 开发：`npm run dev:h5`
 * - 微信开发：`npm run dev:mp-weixin`，开发者工具打开 dist/dev/mp-weixin
 * - 发布 H5：`npm run build:h5`
 * - 发布小程序：`npm run build:mp-weixin`，上传 dist/build/mp-weixin
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const apiTarget = env.VITE_DEV_PROXY_TARGET || "http://localhost:8080";
  const devPort = Number(env.VITE_DEV_PORT || 5173);
  const publicOrigin = env.VITE_DEV_PUBLIC_ORIGIN?.trim();
  const useHttps = env.VITE_DEV_HTTPS === "true";
  const staticDir = path.resolve(__dirname, "static");

  return {
    plugins: [uni(), faviconAtRootPlugin(staticDir), ...(useHttps ? [basicSsl()] : [])],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    // 首页 HeroSection 使用 motion-v；局域网访问时若未预构建会 504 Outdated Optimize Dep
    optimizeDeps: {
      include: ["motion-v"],
    },
    server: {
      host: true,
      port: devPort,
      // 其他电脑用 http://192.168.x.x:5173 访问时，在 .env.development 设置 VITE_DEV_PUBLIC_ORIGIN
      ...(publicOrigin ? { origin: publicOrigin } : {}),
      proxy: {
        "/api": {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
