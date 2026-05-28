package org.example.personalblogback.service.impl;

import org.example.personalblogback.service.FileService;
import org.example.personalblogback.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.storage.type:local}")
    private String storageType;

    @Value("${file.upload.path:}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Autowired
    private OssService ossService;

    // 允许的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    @Override
    public String uploadFile(MultipartFile file) {
        if ("oss".equalsIgnoreCase(storageType)) {
            return ossService.uploadFile(file);
        }
        return uploadToLocal(file);
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        if ("oss".equalsIgnoreCase(storageType)) {
            return ossService.uploadFiles(files);
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadToLocal(file));
        }
        return urls;
    }

    private String uploadToLocal(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 获取上传目录：如果配置为空，则使用项目根目录下的 uploads
            String actualUploadPath = uploadPath;
            if (actualUploadPath == null || actualUploadPath.isEmpty()) {
                actualUploadPath = System.getProperty("user.dir") + "/uploads/";
            }

            // 创建上传目录
            Path uploadDir = Paths.get(actualUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            // 验证文件类型（只允许图片）
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new RuntimeException("只允许上传图片文件（jpg, jpeg, png, gif, webp, bmp）");
            }

            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.write(filePath, file.getBytes());

            // 返回访问URL
            return urlPrefix + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }
}
