package org.example.personalblogback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.personalblogback.common.Result;
import org.example.personalblogback.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 图片上传接口（兼容前端 /upload/image）
     * 返回格式：{ "code": 200, "msg": "图片URL", "data": null }
     */
    @PostMapping("/upload/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("code", 401);
            result.put("msg", "未登录");
            result.put("data", null);
            return result;
        }

        try {
            String url = fileService.uploadFile(file);
            result.put("code", 200);
            result.put("msg", url);  // 图片URL放在msg字段
            result.put("data", null);
            return result;
        } catch (RuntimeException e) {
            result.put("code", 400);
            result.put("msg", e.getMessage());
            result.put("data", null);
            return result;
        }
    }

    /**
     * 通用文件上传接口（需要登录）
     */
    @PostMapping("/api/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            String url = fileService.uploadFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 批量文件上传接口
     */
    @PostMapping("/api/upload/batch")
    public Result<Map<String, List<String>>> uploadFiles(@RequestParam("files") List<MultipartFile> files, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            List<String> urls = fileService.uploadFiles(files);
            Map<String, List<String>> result = new HashMap<>();
            result.put("urls", urls);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
