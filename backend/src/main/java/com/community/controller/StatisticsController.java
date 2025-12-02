package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.dto.StatisticsDTO;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "数据统计", description = "系统数据统计相关接口")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final AuthService authService;
    
    @GetMapping("/system")
    @Operation(summary = "系统统计", description = "获取系统整体统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StatisticsDTO.SystemStats>> getSystemStats() {
        StatisticsDTO.SystemStats stats = statisticsService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/clubs/{clubId}")
    @Operation(summary = "社团统计", description = "获取指定社团的统计数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.ClubStats>> getClubStats(@PathVariable Long clubId) {
        StatisticsDTO.ClubStats stats = statisticsService.getClubStats(clubId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/activities/{activityId}")
    @Operation(summary = "活动统计", description = "获取指定活动的统计数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.ActivityStats>> getActivityStats(@PathVariable Long activityId) {
        StatisticsDTO.ActivityStats stats = statisticsService.getActivityStats(activityId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/my-growth")
    @Operation(summary = "我的成长轨迹", description = "获取当前用户的成长轨迹数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.UserGrowth>> getMyGrowth() {
        User currentUser = authService.getCurrentUser();
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(growth));
    }
    
    @GetMapping("/users/{userId}/growth")
    @Operation(summary = "用户成长轨迹", description = "获取指定用户的成长轨迹数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.UserGrowth>> getUserGrowth(@PathVariable Long userId) {
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(userId);
        return ResponseEntity.ok(ApiResponse.success(growth));
    }
}
