import { ref, onUnmounted, watch, type Ref } from "vue";

/**
 * 倒计时（待支付等），返回 mm:ss 与是否已过期
 */
export function useCountdown(expireAt: Ref<number | undefined>) {
  const display = ref("00:00");
  const expired = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  function tick() {
    const end = expireAt.value;
    if (!end) {
      display.value = "00:00";
      expired.value = false;
      return;
    }
    const diff = end - Date.now();
    if (diff <= 0) {
      display.value = "00:00";
      expired.value = true;
      if (timer) {
        clearInterval(timer);
        timer = null;
      }
      return;
    }
    expired.value = false;
    const m = Math.floor(diff / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    display.value = `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  }

  function start() {
    if (timer) clearInterval(timer);
    tick();
    timer = setInterval(tick, 1000);
  }

  watch(expireAt, start, { immediate: true });

  onUnmounted(() => {
    if (timer) clearInterval(timer);
  });

  return { display, expired };
}
