package com.team13.context.service;

import java.util.Map;

public interface AuthService {

    Map<String, Object> createCaptcha(String clientIp);

    Map<String, Object> sendSmsCode(Map<String, Object> body);

    Map<String, Object> loginLockStatus(String phone);

    Map<String, Object> loginByPhone(Map<String, Object> body);

    Map<String, Object> registerByPhone(Map<String, Object> body);
}
