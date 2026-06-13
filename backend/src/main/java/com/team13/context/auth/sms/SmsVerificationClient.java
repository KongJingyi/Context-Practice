package com.team13.context.auth.sms;

/**
 * 短信验证码发送与核验（开发环境本地存储，生产环境走阿里云短信认证服务 PNVS）。
 */
public interface SmsVerificationClient {

    /**
     * 发送短信验证码。
     *
     * @return 开发模式下可返回 devCode；生产环境为 null
     */
    SmsSendResult send(String phone, String scene);

    /** 核验短信验证码 */
    boolean verify(String phone, String scene, String verifyCode);

    record SmsSendResult(String devCode) {
    }
}
