package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.entity.Scene;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.service.frontend.CoachCatalogService;
import com.team13.context.service.frontend.ScenePlazaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScenePlazaServiceImpl implements ScenePlazaService {

    private final CoachCatalogService coachCatalogService;
    private final SceneMapper sceneMapper;

    @Override
    public Map<String, Object> getPlazaData() {
        List<Map<String, Object>> coaches = coachCatalogService.listCoaches(
                null, null, null, null, null, null);

        List<String> categories = List.of(
                "job", "mgmt", "speech", "pressure", "communication", "conflict");
        Map<String, List<Map<String, Object>>> coachesByCategory = new LinkedHashMap<>();
        for (String category : categories) {
            coachesByCategory.put(
                    category,
                    coachCatalogService.listCoaches(category, null, null, null, null, null));
        }

        List<Scene> scenes = sceneMapper.selectList(
                Wrappers.<Scene>lambdaQuery()
                        .eq(Scene::getStatus, 1)
                        .orderByAsc(Scene::getId));

        List<Map<String, Object>> sceneItems = new ArrayList<>();
        for (Scene scene : scenes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", scene.getId());
            item.put("code", scene.getCode());
            item.put("name", scene.getName());
            item.put("description", scene.getDescription());
            sceneItems.add(item);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sectionTitle", "场景广场");
        data.put("sectionSubtitle",
                "覆盖职场生涯全周期的真实沟通挑战。每位陪练均来自平台认证教练库，可预约 1v1 实战对练。");
        data.put("coachCount", coaches.size());
        data.put("coaches", coaches);
        data.put("coachesByCategory", coachesByCategory);
        data.put("scenes", sceneItems);
        return data;
    }
}
