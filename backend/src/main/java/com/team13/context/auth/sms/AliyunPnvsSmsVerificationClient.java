package com.team13.context.auth.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.team13.context.auth.AuthVerificationStore;
import com.team13.context.config.AuthProperties;
import com.team13.context.config.AuthProperties.AliyunSms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 阿里云「短信认证服务」（号码认证 PNVS / dypnsapi）。
 * 发送：SendSmsVerifyCode；核验：CheckSmsVerifyCode。
 */
@Slf4j
public class AliyunPnvsSmsVerificationClient implements SmsVerificationClient {

    private final AuthProperties authProperties;
    private final AuthVerificationStore verificationStore;
    private final AliyunSms config;
    private volatile Client client;

    public AliyunPnvsSmsVerificationClient(
            AuthProperties authProperties,
            AuthVerificationStore verificationStore) {
        this.authProperties = authProperties;
        this.verificationStore = verificationStore;
        this.config = authProperties.getAliyun();
    }

    @Override
    public SmsSendResult send(String phone, String scene) {
        assertConfigured();
        String schemeName = resolveSchemeName(scene);
        String templateCode = resolveTemplateCode(scene);
        int validMinutes = Math.max(1, authProperties.getSmsExpireSeconds() / 60);
        String templateParam = String.format(
                java.util.Locale.ROOT,
                "{\"code\":\"##code##\",\"min\":\"%d\"}",
                validMinutes);

        try {
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(config.getSignName().trim())
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam)
                    .setCodeLength((long) authProperties.getSmsCodeLength())
                    .setValidTime((long) authProperties.getSmsExpireSeconds())
                    .setInterval((long) authProperties.getSmsMinIntervalSeconds())
                    .setCodeType(1L)
                    .setDuplicatePolicy(1L)
                    .setReturnVerifyCode(authProperties.isSmsDevExpose());

            if (StringUtils.hasText(schemeName)) {
                request.setSchemeName(schemeName);
            }

            SendSmsVerifyCodeResponse response = client().sendSmsVerifyCode(request);
            SendSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(body.getCode())) {
                String msg = body != null ? body.getMessage() : "unknown";
                log.warn("PNVS SendSmsVerifyCode failed phone={} scene={} msg={}", phone, scene, msg);
                throw new IllegalArgumentException(mapSendError(body));
            }

            SendSmsVerifyCodeResponseBody.SendSmsVerifyCodeResponseBodyModel model = body.getModel();
            String outId = model != null ? model.getOutId() : null;
            String verifyCode = model != null ? model.getVerifyCode() : null;
            verificationStore.saveSmsPnvsMeta(
                    phone,
                    scene,
                    outId,
                    schemeName,
                    Duration.ofSeconds(authProperties.getSmsExpireSeconds()));

            log.info("PNVS SMS sent phone={} scene={}", phone, scene);
            String devCode = authProperties.isSmsDevExpose() ? verifyCode : null;
            return new SmsSendResult(devCode);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("PNVS SendSmsVerifyCode error phone={} scene={}", phone, scene, e);
            throw new IllegalArgumentException("短信发送失败，请稍后重试");
        }
    }

    @Override
    public boolean verify(String phone, String scene, String verifyCode) {
        if (StringUtils.hasText(authProperties.getSmsDevFixedCode())
                && authProperties.getSmsDevFixedCode().equals(verifyCode.trim())) {
            return true;
        }
        assertConfigured();
        AuthVerificationStore.SmsPnvsMeta meta = verificationStore.getSmsPnvsMeta(phone, scene);
        String schemeName = meta != null && StringUtils.hasText(meta.schemeName())
                ? meta.schemeName()
                : resolveSchemeName(scene);
        String outId = meta != null ? meta.outId() : null;

        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(verifyCode.trim())
                    .setCaseAuthPolicy(1L);
            if (StringUtils.hasText(schemeName)) {
                request.setSchemeName(schemeName);
            }
            if (StringUtils.hasText(outId)) {
                request.setOutId(outId);
            }

            CheckSmsVerifyCodeResponse response = client().checkSmsVerifyCode(request);
            CheckSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess()) || !"OK".equalsIgnoreCase(body.getCode())) {
                log.warn("PNVS CheckSmsVerifyCode failed phone={} scene={}", phone, scene);
                return false;
            }
            CheckSmsVerifyCodeResponseBody.CheckSmsVerifyCodeResponseBodyModel model = body.getModel();
            boolean pass = model != null && "PASS".equalsIgnoreCase(model.getVerifyResult());
            if (pass) {
                verificationStore.clearSmsPnvsMeta(phone, scene);
            }
            return pass;
        } catch (Exception e) {
            log.error("PNVS CheckSmsVerifyCode error phone={} scene={}", phone, scene, e);
            return false;
        }
    }

    private String mapSendError(SendSmsVerifyCodeResponseBody body) {
        if (body == null) {
            return "短信发送失败，请稍后重试";
        }
        String message = body.getMessage();
        if (StringUtils.hasText(message)) {
            if (message.contains("FREQUENCY") || message.contains("频控")) {
                return "发送过于频繁，请稍后再试";
            }
            if (message.contains("FUNCTION_NOT_OPENED") || message.contains("没有开通")) {
                return "短信认证服务未开通，请在阿里云号码认证控制台开通";
            }
        }
        return "短信发送失败，请稍后重试";
    }

    private String resolveTemplateCode(String scene) {
        if ("register".equalsIgnoreCase(scene)
                && StringUtils.hasText(config.getTemplateCodeRegister())) {
            return config.getTemplateCodeRegister().trim();
        }
        if ("login".equalsIgnoreCase(scene) && StringUtils.hasText(config.getTemplateCodeLogin())) {
            return config.getTemplateCodeLogin().trim();
        }
        return config.getTemplateCode().trim();
    }

    private String resolveSchemeName(String scene) {
        if ("register".equalsIgnoreCase(scene)
                && StringUtils.hasText(config.getSchemeNameRegister())) {
            return config.getSchemeNameRegister().trim();
        }
        if ("login".equalsIgnoreCase(scene) && StringUtils.hasText(config.getSchemeNameLogin())) {
            return config.getSchemeNameLogin().trim();
        }
        return StringUtils.hasText(config.getSchemeName()) ? config.getSchemeName().trim() : "";
    }

    private void assertConfigured() {
        boolean hasTemplate = StringUtils.hasText(config.getTemplateCode())
                || StringUtils.hasText(config.getTemplateCodeLogin());
        if (!StringUtils.hasText(config.getAccessKeyId())
                || !StringUtils.hasText(config.getAccessKeySecret())
                || !StringUtils.hasText(config.getSignName())
                || !hasTemplate) {
            throw new IllegalStateException("阿里云短信认证未配置完整，请检查 app.auth.aliyun.*");
        }
    }

    private Client client() throws Exception {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    Config teaConfig = new Config()
                            .setAccessKeyId(config.getAccessKeyId().trim())
                            .setAccessKeySecret(config.getAccessKeySecret().trim())
                            .setEndpoint("dypnsapi.aliyuncs.com");
                    client = new Client(teaConfig);
                }
            }
        }
        return client;
    }
}
