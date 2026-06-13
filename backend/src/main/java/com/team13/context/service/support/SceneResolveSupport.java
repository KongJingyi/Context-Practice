package com.team13.context.service.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.entity.Scene;
import com.team13.context.mapper.SceneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class SceneResolveSupport {

    private static final String DEFAULT_SCENE_CODE = "INTERVIEW";

    private final SceneMapper sceneMapper;

    public Long resolveSceneId(Object sceneIdRaw, Object sceneCodeRaw) {
        if (sceneIdRaw != null && StringUtils.hasText(String.valueOf(sceneIdRaw))) {
            String text = String.valueOf(sceneIdRaw).trim();
            if (text.chars().allMatch(Character::isDigit)) {
                return Long.parseLong(text);
            }
        }
        if (sceneCodeRaw != null && StringUtils.hasText(String.valueOf(sceneCodeRaw))) {
            Scene byCode = sceneMapper.selectOne(
                    Wrappers.<Scene>lambdaQuery()
                            .eq(Scene::getCode, String.valueOf(sceneCodeRaw).trim())
                            .last("LIMIT 1"));
            if (byCode != null) {
                return byCode.getId();
            }
        }
        Scene fallback = sceneMapper.selectOne(
                Wrappers.<Scene>lambdaQuery()
                        .eq(Scene::getCode, DEFAULT_SCENE_CODE)
                        .last("LIMIT 1"));
        if (fallback != null) {
            return fallback.getId();
        }
        Scene any = sceneMapper.selectOne(
                Wrappers.<Scene>lambdaQuery().eq(Scene::getStatus, 1).last("LIMIT 1"));
        return any != null ? any.getId() : null;
    }
}
