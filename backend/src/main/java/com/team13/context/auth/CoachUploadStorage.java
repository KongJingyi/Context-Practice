package com.team13.context.auth;

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
public class CoachUploadStorage {

    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Path baseDir = Path.of("./data/coach-uploads").toAbsolutePath().normalize();
    private final String urlPrefix = "/api/files/coach/";

    public String store(Long userId, String category, MultipartFile file) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && !ALLOWED.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        String safeCategory = category != null && !category.isBlank() ? category : "misc";
        Path dir = baseDir.resolve(String.valueOf(userId)).resolve(safeCategory).normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(filename).normalize();
            if (!target.startsWith(baseDir)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("文件保存失败", e);
        }
        return urlPrefix + userId + "/" + safeCategory + "/" + filename;
    }

    private static String resolveExtension(String name, String contentType) {
        if (StringUtils.hasText(name) && name.contains(".")) {
            return name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        return ".jpg";
    }

    public Path resolveLocalPath(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith(urlPrefix)) {
            return null;
        }
        String relative = url.substring(urlPrefix.length());
        return baseDir.resolve(relative).normalize();
    }
}
