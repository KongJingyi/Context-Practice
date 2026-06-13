package com.team13.context.trtc;

import com.team13.context.config.TrtcProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.trtc.v20190722.TrtcClient;
import com.tencentcloudapi.trtc.v20190722.models.CloudStorage;
import com.tencentcloudapi.trtc.v20190722.models.CloudVod;
import com.tencentcloudapi.trtc.v20190722.models.CreateCloudRecordingRequest;
import com.tencentcloudapi.trtc.v20190722.models.CreateCloudRecordingResponse;
import com.tencentcloudapi.trtc.v20190722.models.DeleteCloudRecordingRequest;
import com.tencentcloudapi.trtc.v20190722.models.MixLayoutParams;
import com.tencentcloudapi.trtc.v20190722.models.MixTranscodeParams;
import com.tencentcloudapi.trtc.v20190722.models.RecordParams;
import com.tencentcloudapi.trtc.v20190722.models.StorageParams;
import com.tencentcloudapi.trtc.v20190722.models.TencentVod;
import com.tencentcloudapi.trtc.v20190722.models.VideoParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 腾讯云 TRTC 云端录制 API 封装（CreateCloudRecording / DeleteCloudRecording）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentTrtcCloudRecordingClient {

    private final TrtcProperties trtcProperties;
    private final TrtcTokenService trtcTokenService;

    public boolean isEnabled() {
        if (!StringUtils.hasText(trtcProperties.getCloudSecretId())
                || !StringUtils.hasText(trtcProperties.getCloudSecretKey())
                || !StringUtils.hasText(trtcProperties.getSecretKey())
                || trtcProperties.getSdkAppId() == null
                || trtcProperties.getSdkAppId() <= 0) {
            return false;
        }
        if ("cos".equalsIgnoreCase(trtcProperties.getRecordingStorageType())) {
            return StringUtils.hasText(trtcProperties.getCosBucket())
                    && StringUtils.hasText(trtcProperties.getCosRegion());
        }
        return true;
    }

    public String startRecording(Long trainingId, String roomId) {
        if (!isEnabled()) {
            throw new TrtcRecordingException("TRTC 云录制未配置，缺少 SecretId/SecretKey 或 SdkAppId");
        }
        String robotUserId = buildRobotUserId(trainingId);
        String userSig = trtcTokenService.generateUserSig(robotUserId);
        try {
            TrtcClient client = buildClient();
            CreateCloudRecordingRequest request = new CreateCloudRecordingRequest();
            request.setSdkAppId(trtcProperties.getSdkAppId());
            request.setRoomId(roomId);
            request.setRoomIdType(0L);
            request.setUserId(robotUserId);
            request.setUserSig(userSig);
            request.setResourceExpiredHour((long) trtcProperties.getRecordingResourceExpiredHours());

            RecordParams recordParams = new RecordParams();
            recordParams.setRecordMode((long) trtcProperties.getRecordMode());
            recordParams.setStreamType(0L);
            recordParams.setMaxIdleTime((long) trtcProperties.getMaxIdleTimeSec());
            request.setRecordParams(recordParams);
            request.setStorageParams(buildStorageParams());

            if (trtcProperties.getRecordMode() == 2) {
                MixLayoutParams layoutParams = new MixLayoutParams();
                layoutParams.setMixLayoutMode(3L);
                request.setMixLayoutParams(layoutParams);

                MixTranscodeParams transcodeParams = new MixTranscodeParams();
                VideoParams videoParams = new VideoParams();
                videoParams.setWidth(1280L);
                videoParams.setHeight(720L);
                videoParams.setFps(15L);
                videoParams.setBitRate(1500000L);
                videoParams.setGop(2L);
                transcodeParams.setVideoParams(videoParams);
                request.setMixTranscodeParams(transcodeParams);
            }

            CreateCloudRecordingResponse response = client.CreateCloudRecording(request);
            if (!StringUtils.hasText(response.getTaskId())) {
                throw new TrtcRecordingException("CreateCloudRecording 未返回 TaskId");
            }
            log.info("CreateCloudRecording ok trainingId={} roomId={} taskId={}",
                    trainingId, roomId, response.getTaskId());
            return response.getTaskId();
        } catch (TencentCloudSDKException e) {
            throw new TrtcRecordingException("CreateCloudRecording 失败: " + e.getMessage(), e);
        }
    }

    public void stopRecording(String taskId) {
        if (!isEnabled() || !StringUtils.hasText(taskId)) {
            return;
        }
        try {
            TrtcClient client = buildClient();
            DeleteCloudRecordingRequest request = new DeleteCloudRecordingRequest();
            request.setSdkAppId(trtcProperties.getSdkAppId());
            request.setTaskId(taskId);
            client.DeleteCloudRecording(request);
            log.info("DeleteCloudRecording ok taskId={}", taskId);
        } catch (TencentCloudSDKException e) {
            throw new TrtcRecordingException("DeleteCloudRecording 失败: " + e.getMessage(), e);
        }
    }

    private TrtcClient buildClient() {
        Credential credential = new Credential(
                trtcProperties.getCloudSecretId(),
                trtcProperties.getCloudSecretKey());
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("trtc.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new TrtcClient(credential, trtcProperties.getCloudRegion(), clientProfile);
    }

    private StorageParams buildStorageParams() {
        StorageParams storageParams = new StorageParams();
        if ("cos".equalsIgnoreCase(trtcProperties.getRecordingStorageType())) {
            CloudStorage cos = new CloudStorage();
            cos.setVendor(0L);
            cos.setRegion(trtcProperties.getCosRegion());
            cos.setBucket(trtcProperties.getCosBucket());
            cos.setAccessKey(trtcProperties.getCloudSecretId());
            cos.setSecretKey(trtcProperties.getCloudSecretKey());
            if (StringUtils.hasText(trtcProperties.getCosPathPrefix())) {
                cos.setFileNamePrefix(new String[] {trtcProperties.getCosPathPrefix()});
            }
            storageParams.setCloudStorage(cos);
        } else {
            CloudVod cloudVod = new CloudVod();
            TencentVod tencentVod = new TencentVod();
            if (trtcProperties.getVodSubAppId() != null && trtcProperties.getVodSubAppId() > 0) {
                tencentVod.setSubAppId(trtcProperties.getVodSubAppId());
            }
            tencentVod.setExpireTime(0L);
            cloudVod.setTencentVod(tencentVod);
            storageParams.setCloudVod(cloudVod);
        }
        return storageParams;
    }

    private static String buildRobotUserId(Long trainingId) {
        return "recorder_" + trainingId;
    }
}
