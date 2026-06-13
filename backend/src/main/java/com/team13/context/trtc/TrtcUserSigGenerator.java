package com.team13.context.trtc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

/**
 * 腾讯云 TRTC UserSig 生成（TLS 2.0），算法与官方 TLSSigAPIv2 一致。
 */
final class TrtcUserSigGenerator {

    private TrtcUserSigGenerator() {
    }

    static String genUserSig(long sdkAppId, String secretKey, String userId, long expireSeconds) {
        long currTime = System.currentTimeMillis() / 1000;
        String sig = hmacSha256(sdkAppId, userId, currTime, expireSeconds, null, secretKey);
        String json = "{"
                + "\"TLS.ver\":\"2.0\","
                + "\"TLS.identifier\":\"" + escapeJson(userId) + "\","
                + "\"TLS.sdkappid\":" + sdkAppId + ","
                + "\"TLS.expire\":" + expireSeconds + ","
                + "\"TLS.time\":" + currTime + ","
                + "\"TLS.sig\":\"" + escapeJson(sig) + "\""
                + "}";

        Deflater deflater = new Deflater();
        deflater.setInput(json.getBytes(StandardCharsets.UTF_8));
        deflater.finish();
        byte[] buffer = new byte[2048];
        int length = deflater.deflate(buffer);
        deflater.end();
        return base64UrlEncode(Arrays.copyOfRange(buffer, 0, length));
    }

    private static String hmacSha256(
            long sdkAppId, String userId, long currTime, long expire, String base64UserBuf, String key) {
        String content = "TLS.identifier:" + userId + "\n"
                + "TLS.sdkappid:" + sdkAppId + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (base64UserBuf != null) {
            content += "TLS.userbuf:" + base64UserBuf + "\n";
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("生成 TRTC UserSig 失败", e);
        }
    }

    private static String base64UrlEncode(byte[] input) {
        byte[] encoded = Base64.getEncoder().encode(input);
        for (int i = 0; i < encoded.length; i++) {
            switch (encoded[i]) {
                case '+' -> encoded[i] = '*';
                case '/' -> encoded[i] = '-';
                case '=' -> encoded[i] = '_';
                default -> {
                }
            }
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
