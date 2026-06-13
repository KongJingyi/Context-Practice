import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import "./style.css";

const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(router);
app.use(ElementPlus);

// 启动时从 localStorage 恢复登录态（与主前端 token 共享）
import { useUserStore } from "@/store/user";
const userStore = useUserStore();
userStore.hydrateFromStorage();

app.mount("#app");
