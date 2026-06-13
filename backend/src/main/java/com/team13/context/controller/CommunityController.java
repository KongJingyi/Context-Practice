package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.ContentCompatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
@RequiredArgsConstructor
public class CommunityController {

    private final ContentCompatService contentCompatService;

    @GetMapping("/posts")
    public ApiResult<List<Map<String, Object>>> posts(@RequestParam(required = false) String type) {
        return ApiResult.ok(contentCompatService.listPosts(type));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResult<List<Map<String, Object>>> comments(@PathVariable String postId) {
        return ApiResult.ok(contentCompatService.listPostComments(postId));
    }

    @PostMapping("/posts/{postId}/comment")
    public ApiResult<Map<String, Object>> comment(
            @PathVariable String postId,
            @RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.commentPost(postId, body));
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResult<Map<String, Object>> like(
            @PathVariable String postId,
            @RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.likePost(postId, body));
    }

    @GetMapping("/community/expert-tips")
    public ApiResult<List<Map<String, Object>>> expertTips() {
        return ApiResult.ok(contentCompatService.listExpertTips());
    }

    @GetMapping("/community/my-footprint")
    public ApiResult<List<Map<String, Object>>> footprint() {
        return ApiResult.ok(contentCompatService.listMyFootprint());
    }
}
