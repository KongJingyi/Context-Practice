package com.team13.context.trtc;

import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * TRTC 录制回调签名校验：Sign = base64(hmacsha256(key, rawBody))。
 * @see <a href="https://cloud.tencent.com/document/product/647/81113">云端录制回调</a>
 */
public final class TrtcRecordingCallbackSignVerifier {

    private TrtcRecordingCallbackSignVerifier() {
    }

    public static boolean verify(String secretKey, String rawBody, String signHeader) {
        if (!StringUtils.hasText(secretKey)) {
            return true;
        }
        if (!StringUtils.hasText(signHeader) || rawBody == null) {
            return false;
        }
        try {
            String expected = sign(secretKey, rawBody);
            return expected.equals(signHeader.trim());
        } catch (Exception ex) {
            return false;
        }
    }

    public static String sign(String secretKey, String rawBody) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
