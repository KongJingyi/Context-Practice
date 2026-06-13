const TOKEN_KEY = "token";

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || "";
}

export function setTokenStorage(token) {
  if (token) {
    uni.setStorageSync(TOKEN_KEY, token);
  } else {
    uni.removeStorageSync(TOKEN_KEY);
  }
}
