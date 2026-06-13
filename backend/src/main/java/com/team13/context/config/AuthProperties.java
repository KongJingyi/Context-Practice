package com.team13.context.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /** dev=仅日志+可选 devCode；aliyun=真实下发短信 */
    private String smsProvider = "dev";

    /** 开发环境在短信接口响应中返回验证码，便于联调（生产务必 false） */
    private boolean smsDevExpose = true;

    /** 若设置，则任意手机号可用该固定验证码（仅开发联调） */
    private String smsDevFixedCode = "";

    /** 同一手机号两次发送最短间隔（秒） */
    private int smsMinIntervalSeconds = 60;

    /** 短信验证码位数（PNVS 支持 4～8 位） */
    private int smsCodeLength = 6;

    private int smsExpireSeconds = 300;

    private AliyunSms aliyun = new AliyunSms();

    private int captchaExpireSeconds = 300;

    private int captchaLength = 4;

    private int captchaWidth = 120;

    private int captchaHeight = 44;

    private int maxLoginAttempts = 5;

    private int lockMinutes = 15;

    /** 开发环境提交实名后自动通过，便于联调预约流程 */
    private boolean verifyDevAutoApprove = false;

    private VerifyUpload verifyUpload = new VerifyUpload();

    @Data
    public static class VerifyUpload {
        /** 本地存储目录（相对 backend 工作目录） */
        private String storageDir = "./data/verify-uploads";
        private long maxSizeBytes = 5 * 1024 * 1024;
        /** 对外访问 URL 前缀，需与静态资源映射一致 */
        private String urlPrefix = "/api/files/verify/";
    }

    @Data
    public static class AliyunSms {
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String signName = "";
        /** 通用模板（登录/注册未单独配置时使用） */
        private String templateCode = "";
        private String templateCodeLogin = "";
        private String templateCodeRegister = "";
        /** 方案名称，与控制台一致；留空为默认方案 */
        private String schemeName = "";
        private String schemeNameLogin = "";
        private String schemeNameRegister = "";
    }
}
