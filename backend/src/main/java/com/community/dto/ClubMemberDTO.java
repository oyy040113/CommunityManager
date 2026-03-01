package com.community.dto;

import com.community.entity.ClubMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社团成员DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String userName;  // 前端兼容字段，同 realName
    private String studentId;
    private String avatar;
    private String userAvatar;  // 前端兼容字段，同 avatar
    private String userEmail;   // 前端兼容字段
    private String department;
    private Long clubId;
    private String clubName;
    private ClubDTO club;  // 社团信息，用于待审核列表
    private ClubMember.MemberRole role;
    private String position;
    private ClubMember.MemberStatus status;
    private String applicationReason;
    private String rejectReason;
    private Integer activityParticipation;
    private Double contributionScore;
    private LocalDateTime joinedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;  // 申请时间
    
    public static ClubMemberDTO fromEntity(ClubMember member) {
        return ClubMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .realName(member.getUser().getRealName())
                .userName(member.getUser().getRealName())  // 兼容前端
                .studentId(member.getUser().getStudentId())
                .avatar(member.getUser().getAvatar())
                .userAvatar(member.getUser().getAvatar())  // 兼容前端
                .userEmail(member.getUser().getEmail())    // 兼容前端
                .department(member.getUser().getDepartment())
                .clubId(member.getClub().getId())
                .clubName(member.getClub().getName())
                .club(ClubDTO.simpleFromEntity(member.getClub()))  // 社团信息
                .role(member.getRole())
                .position(member.getPosition())
                .status(member.getStatus())
                .applicationReason(member.getApplicationReason())
                .rejectReason(member.getRejectReason())
                .activityParticipation(member.getActivityParticipation())
                .contributionScore(member.getContributionScore())
                .joinedAt(member.getJoinedAt())
                .approvedAt(member.getApprovedAt())
                .createdAt(member.getJoinedAt())  // 申请时间就是 joinedAt
                .build();
    }
    
    /**
     * 加入社团申请
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        
        @NotNull(message = "社团ID不能为空")
        private Long clubId;
        
        @NotBlank(message = "申请理由不能为空")
        private String applicationReason;
    }
    
    /**
     * 审核申请请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewRequest {
        
        @NotNull(message = "是否通过不能为空")
        private Boolean approved;
        
        private String rejectReason;
    }
    
    /**
     * 更新成员角色请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRoleRequest {
        
        @NotNull(message = "成员角色不能为空")
        private ClubMember.MemberRole role;
        
        private String position;
    }
    
    /**
     * 邀请指导老师请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteTeacherRequest {
        
        @NotNull(message = "老师ID不能为空")
        private Long userId;
        
        private String position; // 职位名称，如：指导老师、课程顾问
        
        @NotBlank(message = "邀请理由不能为空")
        private String invitationReason; // 邀请理由
    }
}
