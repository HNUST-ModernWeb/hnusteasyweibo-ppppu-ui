package org.example.personalblogback.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OssService {
    /**
     * 上传单个文件到 OSS
     * @param file 文件
     * @return 文件访问 URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 批量上传文件到 OSS
     * @param files 文件列表
     * @return 文件访问 URL 列表
     */
    List<String> uploadFiles(List<MultipartFile> files);

    /**
     * 删除 OSS 文件
     * @param fileUrl 文件 URL
     */
    void deleteFile(String fileUrl);
}
