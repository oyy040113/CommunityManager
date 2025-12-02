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
    private String studentId;
    private String avatar;
    private String department;
    private Long clubId;
    private String clubName;
    private ClubMember.MemberRole role;
    private String position;
    private ClubMember.MemberStatus status;
    private String applicationReason;
    private String rejectReason;
    private Integer activityParticipation;
    private Double contributionScore;
    private LocalDateTime joinedAt;
    private LocalDateTime approvedAt;
    
    public static ClubMemberDTO fromEntity(ClubMember member) {
        return ClubMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .realName(member.getUser().getRealName())
                .studentId(member.getUser().getStudentId())
                .avatar(member.getUser().getAvatar())
                .department(member.getUser().getDepartment())
                .clubId(member.getClub().getId())
                .clubName(member.getClub().getName())
                .role(member.getRole())
                .position(member.getPosition())
                .status(member.getStatus())
                .applicationReason(member.getApplicationReason())
                .rejectReason(member.getRejectReason())
                .activityParticipation(member.getActivityParticipation())
                .contributionScore(member.getContributionScore())
                .joinedAt(member.getJoinedAt())
                .approvedAt(member.getApprovedAt())
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
}
