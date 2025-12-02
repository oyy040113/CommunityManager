package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传下载相关接口")
public class FileController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "common") String type) throws IOException {
        String fileUrl = fileUploadService.uploadFile(file, type);
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
    }
    
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = fileUploadService.uploadFile(file, "avatars");
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
    }
    
    @PostMapping("/upload/club-logo")
    @Operation(summary = "上传社团Logo", description = "上传社团Logo图片")
    public ResponseEntity<ApiResponse<String>> uploadClubLogo(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = fileUploadService.uploadFile(file, "club-logos");
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
    }
    
    @PostMapping("/upload/activity-cover")
    @Operation(summary = "上传活动封面", description = "上传活动封面图片")
    public ResponseEntity<ApiResponse<String>> uploadActivityCover(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = fileUploadService.uploadFile(file, "activity-covers");
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
    }
    
    @PostMapping("/upload/document")
    @Operation(summary = "上传文档", description = "上传社团文档")
    public ResponseEntity<ApiResponse<String>> uploadDocument(
            @RequestParam("file") MultipartFile file) throws IOException {
        String fileUrl = fileUploadService.uploadFile(file, "documents");
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
    }
    
    @DeleteMapping
    @Operation(summary = "删除文件", description = "删除已上传的文件")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam String fileUrl) throws IOException {
        fileUploadService.deleteFile(fileUrl);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
