package com.team13.context.service.frontend.support;

public final class SceneCategorySupport {

    private SceneCategorySupport() {
    }

    /** 从前端 scene_id（如 job-tech-deep）提取大类 key（job） */
    public static String resolveCategoryId(String sceneId) {
        if (sceneId == null || sceneId.isBlank()) {
            return "";
        }
        String trimmed = sceneId.trim();
        int dash = trimmed.indexOf('-');
        if (dash > 0) {
            return trimmed.substring(0, dash);
        }
        return trimmed;
    }
}
