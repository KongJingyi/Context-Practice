function getBaseURL() {
  let base = "";
  // #ifdef H5
  base = import.meta.env.VITE_API_BASE || "/api";
  // #endif
  // #ifndef H5
  base = import.meta.env.VITE_API_BASE || "";
  // #endif
  return base;
}

/**
 * @param {UniApp.RequestOptions} options
 * @returns {Promise<unknown>}
 */
export function request(options) {
  const token = uni.getStorageSync("token") || "";
  const header = {
    "Content-Type": "application/json",
    ...(options.header || {}),
  };
  if (token) {
    header.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: `${getBaseURL()}${options.url}`,
      header,
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          reject(res);
        }
      },
      fail: reject,
    });
  });
}
