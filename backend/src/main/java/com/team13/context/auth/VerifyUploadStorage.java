package com.team13.context.auth;

import com.team13.context.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerifyUploadStorage {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp");

    private final AuthProperties authProperties;

    public String store(Long userId, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }
        AuthProperties.VerifyUpload config = authProperties.getVerifyUpload();
        if (file.getSize() > config.getMaxSizeBytes()) {
            throw new IllegalArgumentException("图片大小不能超过 "
                    + (config.getMaxSizeBytes() / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("仅支持 JPG、PNG、WEBP 图片");
        }

        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path userDir = Path.of(config.getStorageDir()).resolve(String.valueOf(userId)).toAbsolutePath().normalize();
        try {
            Files.createDirectories(userDir);
            Path target = userDir.resolve(filename).normalize();
            if (!target.startsWith(userDir)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("证件图片保存失败，请稍后重试", e);
        }

        String prefix = config.getUrlPrefix();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + userId + "/" + filename;
    }

    private static String resolveExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.(jpg|jpeg|png|webp)")) {
                return ext;
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }
}
