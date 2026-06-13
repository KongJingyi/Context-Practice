package com.team13.context.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.ApiResult;
import com.team13.context.entity.Scene;
import com.team13.context.mapper.SceneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 训练场景分页查询（仅查询上架场景），供 Uni-app H5 等联调。
 */
@RestController
@RequestMapping("/api/scenes")
@CrossOrigin
@RequiredArgsConstructor
public class SceneController {

    private final SceneMapper sceneMapper;

    /**
     * 分页查询上架的训练场景（RESTful GET）。
     */
    @GetMapping
    public ApiResult<Page<Scene>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 50) {
            size = 10;
        }

        Page<Scene> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Scene> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Scene::getStatus, 1)
                .like(name != null && !name.isBlank(), Scene::getName, name)
                .orderByDesc(Scene::getCreatedAt);

        Page<Scene> result = sceneMapper.selectPage(pageParam, wrapper);
        return ApiResult.ok(result);
    }
}
