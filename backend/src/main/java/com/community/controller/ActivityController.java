package com.community.controller;

import com.community.dto.*;
import com.community.entity.Activity;
import com.community.entity.ActivityRegistration;
import com.community.entity.User;
import com.community.service.ActivityService;
import com.community.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "社团活动管理相关接口")
public class ActivityController {
    
    private final ActivityService activityService;
    private final AuthService authService;
    
    @PostMapping
    @Operation(summary = "创建活动", description = "创建新活动（需要管理员审批）")
    public ResponseEntity<ApiResponse<ActivityDTO>> createActivity(
            @Valid @RequestBody ActivityDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityDTO activity = activityService.createActivity(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("活动创建成功，请等待管理员审批", activity));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新活动", description = "更新活动信息")
    public ResponseEntity<ApiResponse<ActivityDTO>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityDTO.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityDTO activity = activityService.updateActivity(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("活动更新成功", activity));
    }
    
    @PutMapping("/{id}/publish")
    @Operation(summary = "发布活动", description = "发布草稿状态的活动")
    public ResponseEntity<ApiResponse<ActivityDTO>> publishActivity(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        ActivityDTO activity = activityService.publishActivity(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("活动发布成功", activity));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动", description = "删除指定活动")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        activityService.deleteActivity(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("活动删除成功"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情", description = "根据ID获取活动详细信息")
    public ResponseEntity<ApiResponse<ActivityDTO>> getActivityById(@PathVariable Long id) {
        User currentUser = null;
        try {
            currentUser = authService.getCurrentUser();
        } catch (Exception ignored) {}
        
        ActivityDTO activity = activityService.getActivityById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(activity));
    }
    
    @GetMapping
    @Operation(summary = "搜索活动", description = "根据条件搜索活动")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> searchActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Activity.ActivityStatus status,
            Pageable pageable) {
        Page<ActivityDTO> activities = activityService.searchActivities(keyword, clubId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "即将开始的活动", description = "获取即将开始的活动列表")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getUpcomingActivities(Pageable pageable) {
        Page<ActivityDTO> activities = activityService.getUpcomingActivities(pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/club/{clubId}")
    @Operation(summary = "社团活动列表", description = "获取指定社团的活动列表")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getClubActivities(
            @PathVariable Long clubId,
            Pageable pageable) {
        Page<ActivityDTO> activities = activityService.getClubActivities(clubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    // 活动报名
    
    @PostMapping("/{id}/register")
    @Operation(summary = "报名活动", description = "报名参加活动")
    public ResponseEntity<ApiResponse<ActivityRegistrationDTO>> registerActivity(
            @PathVariable Long id,
            @RequestBody(required = false) ActivityRegistrationDTO.RegisterRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityRegistrationDTO registration = activityService.registerActivity(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("报名成功", registration));
    }
    
    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "取消报名", description = "取消活动报名")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        activityService.cancelRegistration(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("报名已取消"));
    }
    
    @PostMapping("/{id}/check-in")
    @Operation(summary = "活动签到", description = "扫码签到")
    public ResponseEntity<ApiResponse<ActivityRegistrationDTO>> checkIn(
            @PathVariable Long id,
            @RequestBody ActivityRegistrationDTO.CheckInRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityRegistrationDTO registration = activityService.checkIn(id, request.getQrCode(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("签到成功", registration));
    }
    
    @PostMapping("/{id}/manual-check-in/{userId}")
    @Operation(summary = "手动签到", description = "社团负责人为参与者手动签到")
    public ResponseEntity<ApiResponse<ActivityRegistrationDTO>> manualCheckIn(
            @PathVariable Long id,
            @PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        ActivityRegistrationDTO registration = activityService.manualCheckIn(id, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("签到成功", registration));
    }
    
    @GetMapping({"/{id}/registrations", "/{id}/participants"})
    @Operation(summary = "获取报名列表", description = "获取活动的报名列表")
    public ResponseEntity<ApiResponse<Page<ActivityRegistrationDTO>>> getActivityRegistrations(
            @PathVariable Long id,
            @RequestParam(required = false) ActivityRegistration.RegistrationStatus status,
            Pageable pageable) {
        Page<ActivityRegistrationDTO> registrations = activityService.getActivityRegistrations(id, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
    
    @GetMapping("/my-history")
    @Operation(summary = "我的活动历史", description = "获取当前用户的活动参与历史")
    public ResponseEntity<ApiResponse<Page<ActivityRegistrationDTO>>> getMyActivityHistory(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<ActivityRegistrationDTO> history = activityService.getUserActivityHistory(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/my-registrations")
    @Operation(summary = "我的报名", description = "获取当前用户的报名列表")
    public ResponseEntity<ApiResponse<Page<ActivityRegistrationDTO>>> getMyRegistrations(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<ActivityRegistrationDTO> regs = activityService.getUserActivityHistory(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(regs));
    }

    @GetMapping("/my-feedbacks")
    @Operation(summary = "我的评价", description = "获取当前用户提交的活动评价")
    public ResponseEntity<ApiResponse<Page<ActivityFeedbackDTO>>> getMyFeedbacks(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        Page<ActivityFeedbackDTO> feedbacks = activityService.getUserFeedbacks(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }
    
    // 活动反馈
    
    @PostMapping("/feedback")
    @Operation(summary = "提交活动反馈", description = "对参与的活动提交评价")
    public ResponseEntity<ApiResponse<ActivityFeedbackDTO>> submitFeedback(
            @Valid @RequestBody ActivityFeedbackDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityFeedbackDTO feedback = activityService.submitFeedback(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("反馈提交成功", feedback));
    }

    @PostMapping("/{activityId}/feedback")
    @Operation(summary = "提交活动反馈（路径参数）", description = "通过活动ID路径参数对参与活动提交评价")
    public ResponseEntity<ApiResponse<ActivityFeedbackDTO>> submitFeedbackByPath(
            @PathVariable Long activityId,
            @RequestBody ActivityFeedbackDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        // Fill activityId from path
        request.setActivityId(activityId);
        ActivityFeedbackDTO feedback = activityService.submitFeedback(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("反馈提交成功", feedback));
    }
    
    @GetMapping("/{id}/feedbacks")
    @Operation(summary = "获取活动反馈", description = "获取活动的评价列表")
    public ResponseEntity<ApiResponse<Page<ActivityFeedbackDTO>>> getActivityFeedbacks(
            @PathVariable Long id,
            Pageable pageable) {
        Page<ActivityFeedbackDTO> feedbacks = activityService.getActivityFeedbacks(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(feedbacks));
    }
    
    // 活动总结
    
    @PostMapping("/{id}/summary")
    @Operation(summary = "提交活动总结", description = "活动结束后提交总结")
    public ResponseEntity<ApiResponse<ActivityDTO>> submitSummary(
            @PathVariable Long id,
            @Valid @RequestBody ActivityDTO.SummaryRequest request) {
        User currentUser = authService.getCurrentUser();
        ActivityDTO activity = activityService.submitSummary(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("总结提交成功", activity));
    }
    
    @GetMapping("/{id}/stats")
    @Operation(summary = "活动统计", description = "获取活动的统计数据")
    public ResponseEntity<ApiResponse<ActivityService.ActivityStatsDTO>> getActivityStats(@PathVariable Long id) {
        ActivityService.ActivityStatsDTO stats = activityService.generateActivityStats(id);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    // ==================== 管理员接口 ====================
    
    @GetMapping("/admin/pending")
    @Operation(summary = "获取待审批活动", description = "管理员/指导老师获取待审批的活动列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getPendingActivities(Pageable pageable) {
        Page<ActivityDTO> activities = activityService.getPendingActivities(pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/admin/search")
    @Operation(summary = "管理员搜索活动", description = "管理员/指导老师根据多条件搜索活动")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> searchActivitiesAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Activity.ActivityStatus status,
            @RequestParam(required = false) Activity.ApprovalStatus approvalStatus,
            Pageable pageable) {
        Page<ActivityDTO> activities = activityService.searchActivitiesAdmin(
                keyword, clubId, status, approvalStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @PutMapping("/admin/{id}/approve")
    @Operation(summary = "审批活动", description = "管理员/指导老师审批活动")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ActivityDTO>> approveActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityDTO.ApprovalRequest request) {
        User admin = authService.getCurrentUser();
        ActivityDTO activity = activityService.approveActivity(id, request, admin);
        return ResponseEntity.ok(ApiResponse.success(
                request.getApproved() ? "活动审批通过" : "活动审批拒绝", activity));
    }
}
