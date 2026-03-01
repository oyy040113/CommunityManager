package com.community.controller;

import com.community.dto.*;
import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社团控制器
 */
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "社团管理", description = "社团信息管理相关接口")
public class ClubController {
    
    private final ClubService clubService;
    private final AuthService authService;
    private final ActivityService activityService;
    private final AnnouncementService announcementService;
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "创建社团", description = "用户创建新社团（需要管理员审批）")
    public ResponseEntity<ApiResponse<ClubDTO>> createClub(@Valid @RequestBody ClubDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubDTO club = clubService.createClub(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("社团创建申请已提交，请等待管理员审批", club));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新社团", description = "更新社团信息")
    public ResponseEntity<ApiResponse<ClubDTO>> updateClub(
            @PathVariable Long id,
            @Valid @RequestBody ClubDTO.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubDTO club = clubService.updateClub(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("社团更新成功", club));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除社团", description = "删除社团（社团创建者或管理员）")
    public ResponseEntity<ApiResponse<Void>> deleteClub(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        clubService.deleteClub(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("社团删除成功"));
    }
    
    @PostMapping("/{id}/transfer")
    @Operation(summary = "转让社团", description = "社团创建者将社团转让给其他成员")
    public ResponseEntity<ApiResponse<ClubDTO>> transferClub(
            @PathVariable Long id,
            @Valid @RequestBody ClubDTO.TransferRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubDTO club = clubService.transferClub(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("社团转让成功", club));
    }
    
    @PostMapping("/{id}/set-admin")
    @Operation(summary = "设置社团管理员", description = "社团创建者可以设置其他成员为社团管理员")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> setClubAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ClubDTO.SetAdminRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.setClubAdmin(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                request.getIsAdmin() ? "已设置为社团管理员" : "已取消社团管理员", member));
    }
    
    @DeleteMapping("/{clubId}/members/{userId}")
    @Operation(summary = "移除成员", description = "将成员从社团中移除")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long clubId,
            @PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        clubService.removeMember(clubId, userId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("成员已移除"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取社团详情", description = "根据ID获取社团详细信息")
    public ResponseEntity<ApiResponse<ClubDTO>> getClubById(@PathVariable Long id) {
        ClubDTO club = clubService.getClubById(id);
        return ResponseEntity.ok(ApiResponse.success(club));
    }
    
    @GetMapping
    @Operation(summary = "搜索社团", description = "根据条件搜索社团")
    public ResponseEntity<ApiResponse<Page<ClubDTO>>> searchClubs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Club.ClubType type,
            Pageable pageable) {
        Page<ClubDTO> clubs = clubService.searchClubs(keyword, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @GetMapping("/top")
    @Operation(summary = "获取热门社团", description = "获取活跃度最高的社团列表")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> getTopActiveClubs(
            @RequestParam(defaultValue = "10") int limit) {
        List<ClubDTO> clubs = clubService.getTopActiveClubs(limit);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @GetMapping("/my-clubs")
    @Operation(summary = "我的社团", description = "获取当前用户加入的社团")
    public ResponseEntity<ApiResponse<List<ClubMemberDTO>>> getMyClubs() {
        User currentUser = authService.getCurrentUser();
        List<ClubMemberDTO> memberships = clubService.getUserMemberships(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(memberships));
    }
    
    @GetMapping("/my-managed")
    @Operation(summary = "我管理的社团", description = "获取当前用户管理的社团")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> getMyManagedClubs() {
        User currentUser = authService.getCurrentUser();
        List<ClubDTO> clubs = clubService.getClubsByLeader(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @GetMapping("/my-created")
    @Operation(summary = "我创建的社团", description = "获取当前用户创建的所有社团（含待审批）")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> getMyCreatedClubs() {
        User currentUser = authService.getCurrentUser();
        List<ClubDTO> clubs = clubService.getClubsByCreator(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @GetMapping("/pending-approvals")
    @Operation(summary = "待审批申请", description = "获取当前用户管理的社团的所有待审批申请")
    public ResponseEntity<ApiResponse<List<ClubMemberDTO>>> getPendingApprovals() {
        User currentUser = authService.getCurrentUser();
        List<ClubMemberDTO> applications = clubService.getPendingApprovalsForLeader(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }
    
    // 社团成员管理
    
    @PostMapping("/join")
    @Operation(summary = "申请加入社团", description = "提交加入社团的申请（普通用户和社团负责人）")
    @PreAuthorize("hasAnyRole('USER', 'CLUB_LEADER', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> applyToJoinClub(
            @Valid @RequestBody ClubMemberDTO.JoinRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.applyToJoinClub(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("申请已提交", member));
    }
    
    @PostMapping("/{clubId}/join")
    @Operation(summary = "申请加入社团", description = "通过路径参数提交加入社团的申请（普通用户和社团负责人）")
    @PreAuthorize("hasAnyRole('USER', 'CLUB_LEADER', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> applyToJoinClubByPath(
            @PathVariable Long clubId,
            @RequestBody(required = false) ClubMemberDTO.JoinRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO.JoinRequest joinRequest = ClubMemberDTO.JoinRequest.builder()
                .clubId(clubId)
                .applicationReason(request != null ? request.getApplicationReason() : "申请加入")
                .build();
        ClubMemberDTO member = clubService.applyToJoinClub(joinRequest, currentUser);
        return ResponseEntity.ok(ApiResponse.success("申请已提交", member));
    }

    @GetMapping("/my-applications")
    @Operation(summary = "我的申请", description = "获取当前用户的待审核申请")
    public ResponseEntity<ApiResponse<List<ClubMemberDTO>>> getMyApplications() {
        User currentUser = authService.getCurrentUser();
        List<ClubMemberDTO> applications = clubService.getUserPendingApplications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(applications));
    }
    
    @GetMapping("/{clubId}/members")
    @Operation(summary = "获取社团成员", description = "获取社团成员列表")
    public ResponseEntity<ApiResponse<Page<ClubMemberDTO>>> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "APPROVED") ClubMember.MemberStatus status,
            Pageable pageable) {
        Page<ClubMemberDTO> members = clubService.getClubMembers(clubId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
    
    @GetMapping("/{clubId}/applications")
    @Operation(summary = "获取待审核申请", description = "获取社团待审核的加入申请")
    public ResponseEntity<ApiResponse<Page<ClubMemberDTO>>> getPendingApplications(
            @PathVariable Long clubId,
            Pageable pageable) {
        Page<ClubMemberDTO> applications = clubService.getPendingApplications(clubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }
    
    @PutMapping("/members/{memberId}/review")
    @Operation(summary = "审核申请", description = "审核加入社团的申请")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> reviewApplication(
            @PathVariable Long memberId,
            @Valid @RequestBody ClubMemberDTO.ReviewRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.reviewApplication(memberId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                request.getApproved() ? "申请已通过" : "申请已拒绝", member));
    }
    
    @PutMapping("/members/{memberId}/role")
    @Operation(summary = "更新成员角色", description = "更新社团成员的角色")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> updateMemberRole(
            @PathVariable Long memberId,
            @Valid @RequestBody ClubMemberDTO.UpdateRoleRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.updateMemberRole(memberId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", member));
    }
    
    @DeleteMapping("/{clubId}/leave")
    @Operation(summary = "退出社团", description = "退出已加入的社团")
    public ResponseEntity<ApiResponse<Void>> leaveClub(@PathVariable Long clubId) {
        User currentUser = authService.getCurrentUser();
        clubService.leaveClub(clubId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("已退出社团"));
    }
    
    @PostMapping("/{clubId}/invite-teacher")
    @Operation(summary = "邀请指导老师", description = "社团负责人邀请指导老师（老师需要同意邀请）")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> inviteTeacher(
            @PathVariable Long clubId,
            @Valid @RequestBody ClubMemberDTO.InviteTeacherRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.inviteTeacher(clubId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("邀请已发送", member));
    }
    
    @PutMapping("/teacher-invitations/{memberId}/accept")
    @Operation(summary = "接受邀请成为指导老师", description = "指导老师接受社团邀请，成为该社团的指导老师")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> acceptTeacherInvitation(
            @PathVariable Long memberId) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.acceptTeacherInvitation(memberId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("邀请已接受，已成为社团指导老师", member));
    }
    
    @PutMapping("/teacher-invitations/{memberId}/reject")
    @Operation(summary = "拒绝邀请", description = "指导老师拒绝社团邀请")
    public ResponseEntity<ApiResponse<Void>> rejectTeacherInvitation(
            @PathVariable Long memberId) {
        User currentUser = authService.getCurrentUser();
        clubService.rejectTeacherInvitation(memberId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("邀请已拒绝"));
    }
    
    @GetMapping("/teacher-invitations")
    @Operation(summary = "获取指导老师邀请", description = "获取当前用户收到的指导老师邀请列表")
    public ResponseEntity<ApiResponse<List<ClubMemberDTO>>> getTeacherInvitations() {
        User currentUser = authService.getCurrentUser();
        List<ClubMemberDTO> invitations = clubService.getTeacherInvitations(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }
    
    @GetMapping("/{clubId}/activities")
    @Operation(summary = "获取社团活动", description = "获取指定社团的活动列表")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getClubActivities(
            @PathVariable Long clubId,
            Pageable pageable) {
        Page<ActivityDTO> activities = activityService.getClubActivities(clubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
    
    @GetMapping("/{clubId}/announcements")
    @Operation(summary = "获取社团公告", description = "获取指定社团的公告列表")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getClubAnnouncements(
            @PathVariable Long clubId,
            Pageable pageable) {
        Page<AnnouncementDTO> announcements = announcementService.getClubAnnouncements(clubId, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }
    
    // ==================== 管理员接口 ====================
    
    @GetMapping("/admin/pending")
    @Operation(summary = "获取待审批社团", description = "管理员或指导老师获取待审批的社团列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<ClubDTO>>> getPendingClubs(Pageable pageable) {
        Page<ClubDTO> clubs = clubService.getPendingClubs(pageable);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @GetMapping("/admin/search")
    @Operation(summary = "管理员搜索社团", description = "管理员或指导老师根据多条件搜索社团")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<ClubDTO>>> searchClubsAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Club.ClubType type,
            @RequestParam(required = false) Club.ClubStatus status,
            Pageable pageable) {
        Page<ClubDTO> clubs = clubService.searchClubsAdmin(keyword, type, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }
    
    @PostMapping("/admin/create")
    @Operation(summary = "管理员创建社团", description = "管理员直接创建社团（无需审批）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ClubDTO>> createClubByAdmin(
            @Valid @RequestBody ClubDTO.CreateRequest request,
            @RequestParam Long leaderId) {
        User admin = authService.getCurrentUser();
        ClubDTO club = clubService.createClubByAdmin(request, admin, leaderId, userService);
        return ResponseEntity.ok(ApiResponse.success("社团创建成功", club));
    }
    
    @PutMapping("/admin/{id}/approve")
    @Operation(summary = "审批社团", description = "管理员或指导老师审批社团创建申请")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClubDTO>> approveClub(
            @PathVariable Long id,
            @Valid @RequestBody ClubDTO.ApprovalRequest request) {
        User admin = authService.getCurrentUser();
        ClubDTO club = clubService.approveClub(id, request, admin);
        return ResponseEntity.ok(ApiResponse.success(
                request.getApproved() ? "社团审批通过" : "社团审批拒绝", club));
    }
    
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "管理员删除社团", description = "管理员强制删除社团")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteClubByAdmin(@PathVariable Long id) {
        User admin = authService.getCurrentUser();
        clubService.deleteClub(id, admin);
        return ResponseEntity.ok(ApiResponse.success("社团删除成功"));
    }
}
