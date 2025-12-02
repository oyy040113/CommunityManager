package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.entity.Notification;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "用户通知管理相关接口")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final AuthService authService;
    
    @GetMapping
    @Operation(summary = "获取通知列表", description = "获取当前用户的通知列表")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<Notification> notifications = notificationService.getUserNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知", description = "获取当前用户的未读通知")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
    
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读数量", description = "获取当前用户的未读通知数量")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        User currentUser = authService.getCurrentUser();
        long count = notificationService.countUnreadNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @PutMapping("/{id}/read")
    @Operation(summary = "标记为已读", description = "将指定通知标记为已读")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("已标记为已读"));
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有通知标记为已读")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("已全部标记为已读"));
    }
}
