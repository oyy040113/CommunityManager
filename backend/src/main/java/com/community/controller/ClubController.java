package com.community.controller;

import com.community.dto.*;
import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.ClubService;
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
 * 社团控制器
 */
@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
@Tag(name = "社团管理", description = "社团信息管理相关接口")
public class ClubController {
    
    private final ClubService clubService;
    private final AuthService authService;
    
    @PostMapping
    @Operation(summary = "创建社团", description = "创建新社团")
    public ResponseEntity<ApiResponse<ClubDTO>> createClub(@Valid @RequestBody ClubDTO.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubDTO club = clubService.createClub(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("社团创建成功", club));
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
    
    // 社团成员管理
    
    @PostMapping("/join")
    @Operation(summary = "申请加入社团", description = "提交加入社团的申请")
    public ResponseEntity<ApiResponse<ClubMemberDTO>> applyToJoinClub(
            @Valid @RequestBody ClubMemberDTO.JoinRequest request) {
        User currentUser = authService.getCurrentUser();
        ClubMemberDTO member = clubService.applyToJoinClub(request, currentUser);
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
}
