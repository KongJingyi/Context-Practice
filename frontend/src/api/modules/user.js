import { request } from "@/api/request.js";

export function loginByCode(code) {
  return request({
    url: "/v1/auth/login",
    method: "POST",
    data: { code },
  });
}

export function fetchUserProfile() {
  return request({
    url: "/v1/user/profile",
    method: "GET",
  });
}
