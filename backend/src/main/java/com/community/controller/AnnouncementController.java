package com.community.controller;

import com.community.dto.AnnouncementDTO;
import com.community.dto.ApiResponse;
import com.community.entity.User;
import com.community.service.AnnouncementService;
import com.community.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
@Tag(name = "公告管理", description = "社团公告管理相关接口")
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    private final AuthService authService;
    
    @PostMapping
    @Operation(summary = "创建公告", description = "发布新公告")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> createAnnouncement(
            @Valid @RequestBody AnnouncementDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        AnnouncementDTO announcement = announcementService.createAnnouncement(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("公告发布成功", announcement));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新公告", description = "更新公告内容")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementDTO.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        AnnouncementDTO announcement = announcementService.updateAnnouncement(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("公告更新成功", announcement));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情", description = "根据ID获取公告详细信息")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> getAnnouncementById(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }
    
    @GetMapping("/club/{clubId}")
    @Operation(summary = "获取社团公告", description = "获取指定社团的公告列表")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getClubAnnouncements(
            @PathVariable Long clubId,
            Pageable pageable) {
        Page<AnnouncementDTO> announcements = announcementService.getClubAnnouncements(clubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }
    
    @GetMapping("/my-related")
    @Operation(summary = "我的相关公告", description = "获取当前用户所加入社团的公告")
    public ResponseEntity<ApiResponse<List<AnnouncementDTO>>> getMyRelatedAnnouncements(
            @RequestParam(defaultValue = "20") int limit) {
        User currentUser = authService.getCurrentUser();
        List<AnnouncementDTO> announcements = announcementService.getUserRelatedAnnouncements(
                currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告", description = "删除指定公告")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        announcementService.deleteAnnouncement(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("公告删除成功"));
    }
}
