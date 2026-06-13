/**
 * 简单防抖
 */
export function debounce<T extends (...args: unknown[]) => void>(fn: T, wait = 400) {
  let timer: ReturnType<typeof setTimeout> | null = null;
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      timer = null;
      fn(...args);
    }, wait);
  };
}
