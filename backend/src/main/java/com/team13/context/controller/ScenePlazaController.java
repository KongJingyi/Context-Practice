package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.ScenePlazaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/scene-plaza")
@CrossOrigin
@RequiredArgsConstructor
public class ScenePlazaController {

    private final ScenePlazaService scenePlazaService;

    /** 场景广场：上架场景 + 数据库中已启用的陪练列表 */
    @GetMapping
    public ApiResult<Map<String, Object>> plaza() {
        return ApiResult.ok(scenePlazaService.getPlazaData());
    }
}
