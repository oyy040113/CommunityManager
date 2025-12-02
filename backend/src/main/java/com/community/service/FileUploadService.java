package com.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.allowed-types:jpg,jpeg,png,gif,pdf,doc,docx}")
    private String allowedTypes;
    
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String subDir) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 创建目录
        Path uploadPath = Paths.get(uploadDir, subDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("文件上传成功: {}", filePath);
        
        // 返回访问路径
        return "/uploads/" + subDir + "/" + newFilename;
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }
        
        String relativePath = fileUrl.substring("/uploads/".length());
        Path filePath = Paths.get(uploadDir, relativePath);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("文件删除成功: {}", filePath);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        
        if (!allowed.contains(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
