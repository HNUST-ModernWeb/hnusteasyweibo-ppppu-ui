package org.example.personalblogback.config;

import org.example.personalblogback.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.path:}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    /**
     * 获取实际上传目录
     */
    private String getActualUploadPath() {
        if (uploadPath == null || uploadPath.isEmpty()) {
            return System.getProperty("user.dir") + "/uploads/";
        }
        return uploadPath;
    }

    /**
     * 应用启动时创建上传目录
     */
    @PostConstruct
    public void init() {
        File uploadDir = new File(getActualUploadPath());
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + getActualUploadPath());
    }
}
