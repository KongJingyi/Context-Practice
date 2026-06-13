import path from "node:path";
import { defineConfig, loadEnv } from "vite";
import uni from "@dcloudio/vite-plugin-uni";
import basicSsl from "@vitejs/plugin-basic-ssl";

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

  return {
    plugins: [uni(), ...(useHttps ? [basicSsl()] : [])],
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
