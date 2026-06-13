package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.ContentCompatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin
@RequiredArgsConstructor
public class ComplaintFavoriteController {

    private final ContentCompatService contentCompatService;

    @PostMapping("/complaints/open")
    public ApiResult<Map<String, Object>> openComplaint(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.openComplaint(body));
    }

    @GetMapping("/complaints/{complaintId}/status")
    public ApiResult<Map<String, Object>> complaintStatus(@PathVariable String complaintId) {
        return ApiResult.ok(contentCompatService.getComplaintStatus(complaintId));
    }

    @GetMapping("/user/favorites")
    public ApiResult<Map<String, Object>> favorites(@RequestParam(required = false) String type) {
        return ApiResult.ok(contentCompatService.listFavorites(type));
    }

    @DeleteMapping("/user/favorites/{type}/{id}")
    public ApiResult<Map<String, Object>> removeFavorite(
            @PathVariable String type,
            @PathVariable String id) {
        return ApiResult.ok(contentCompatService.removeFavorite(type, id));
    }
}
