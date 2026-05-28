package org.example.personalblogback.service.impl;

import com.aliyun.oss.OSS;
import org.example.personalblogback.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "uploads/" + UUID.randomUUID().toString() + extension;

            // 上传文件到 OSS
            ossClient.putObject(bucketName, filename, file.getInputStream());

            // 返回文件访问 URL
            return "https://" + bucketName + "." + endpoint + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // 从 URL 中提取文件名
            String filename = fileUrl.substring(fileUrl.indexOf("uploads/"));
            ossClient.deleteObject(bucketName, filename);
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }
}
