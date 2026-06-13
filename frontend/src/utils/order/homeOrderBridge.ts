/** 从首页跳转「我的订单」时高亮指定订单 */

const KEY = "ctx_home_focus_order";

export function setHomeOrderFocus(orderId: string) {
  uni.setStorageSync(KEY, orderId);
}

export function consumeHomeOrderFocus(): string | null {
  try {
    const id = uni.getStorageSync(KEY);
    if (!id) return null;
    uni.removeStorageSync(KEY);
    return String(id);
  } catch {
    return null;
  }
}
