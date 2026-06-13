package com.team13.context.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.trtc")
public class TrtcProperties {

    /** 腾讯云 TRTC SDKAppId；未配置时使用开发占位值 */
    private Long sdkAppId = 0L;

    /** 腾讯云密钥；为空时签发 DEV_STUB 凭证供联调 UI */
    private String secretKey = "";

    /** UserSig 有效期（秒） */
    private long expireSeconds = 86400L;

    /** 云录制：腾讯云 API SecretId（CAM 密钥，与 TRTC 控制台密钥不同） */
    private String cloudSecretId = "";

    /** 云录制：腾讯云 API SecretKey */
    private String cloudSecretKey = "";

    /** 云录制 API 地域：ap-guangzhou / ap-shanghai / ap-beijing */
    private String cloudRegion = "ap-guangzhou";

    /**
     * 录制文件存储：vod（云点播，默认）或 cos（对象存储）。
     * 需在 TRTC 控制台开通对应能力并配置回调。
     */
    private String recordingStorageType = "vod";

    /** VOD 子应用 ID（2023-12-25 后新开通点播必填，0 表示主应用） */
    private Long vodSubAppId = 0L;

    /** COS 存储桶，recordingStorageType=cos 时必填，如 trtc-record-1250000000 */
    private String cosBucket = "";

    /** COS 地域，如 ap-guangzhou */
    private String cosRegion = "ap-guangzhou";

    /** COS 路径前缀 */
    private String cosPathPrefix = "context-practice/recordings";

    /** 录制模式：1 单流，2 合流（推荐双人陪练） */
    private int recordMode = 2;

    /** 房间内无人上麦超过该秒数后自动结束录制任务 */
    private int maxIdleTimeSec = 120;

    /** 录制任务 API 调用有效期（小时），默认 72 */
    private int recordingResourceExpiredHours = 72;

    /** 开发/演示用回放 MP4 地址（未启用云 API 或回调前占位） */
    private String devRecordingUrl =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    /** 回放保留天数 */
    private int recordingRetentionDays = 7;

    /** TRTC 控制台「录制回调」密钥，用于校验回调签名（可选） */
    private String recordingCallbackSecret = "";
}
