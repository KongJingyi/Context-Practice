package com.team13.context.trtc;

import com.team13.context.config.TrtcProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 签发腾讯云 TRTC UserSig。未配置密钥时返回 DEV_STUB 凭证，便于前后端 UI 联调。
 */
@Service
@RequiredArgsConstructor
public class TrtcTokenService {

    private final TrtcProperties trtcProperties;

    public long resolveSdkAppId() {
        Long configured = trtcProperties.getSdkAppId();
        if (configured != null && configured > 0) {
            return configured;
        }
        return 1_400_000_000L;
    }

    public String generateUserSig(String trtcUserId) {
        if (!StringUtils.hasText(trtcProperties.getSecretKey())) {
            return "DEV_STUB_" + trtcUserId;
        }
        return TrtcUserSigGenerator.genUserSig(
                resolveSdkAppId(),
                trtcProperties.getSecretKey(),
                trtcUserId,
                trtcProperties.getExpireSeconds());
    }

    public static String toTrtcUserId(Long userId, String role) {
        if ("COACH".equals(role)) {
            return "c_" + userId;
        }
        return "u_" + userId;
    }
}
