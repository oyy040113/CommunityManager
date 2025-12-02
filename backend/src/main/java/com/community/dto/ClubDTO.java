package com.community.dto;

import com.community.entity.Club;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 社团DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
    
    private Long id;
    private String name;
    private Club.ClubType type;
    private String purpose;
    private String description;
    private String logo;
    private String coverImage;
    private String contactEmail;
    private String contactPhone;
    private String location;
    private String documentUrl;
    private Long leaderId;
    private String leaderName;
    private Integer memberCount;
    private Integer activityCount;
    private Double activityScore;
    private Club.ClubStatus status;
    private LocalDateTime createdAt;
    
    public static ClubDTO fromEntity(Club club) {
        return ClubDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .type(club.getType())
                .purpose(club.getPurpose())
                .description(club.getDescription())
                .logo(club.getLogo())
                .coverImage(club.getCoverImage())
                .contactEmail(club.getContactEmail())
                .contactPhone(club.getContactPhone())
                .location(club.getLocation())
                .documentUrl(club.getDocumentUrl())
                .leaderId(club.getLeader() != null ? club.getLeader().getId() : null)
                .leaderName(club.getLeader() != null ? club.getLeader().getRealName() : null)
                .memberCount(club.getMemberCount())
                .activityCount(club.getActivityCount())
                .activityScore(club.getActivityScore())
                .status(club.getStatus())
                .createdAt(club.getCreatedAt())
                .build();
    }
    
    /**
     * 创建社团请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        
        @NotBlank(message = "社团名称不能为空")
        @Size(max = 100, message = "社团名称长度不能超过100个字符")
        private String name;
        
        @NotNull(message = "社团类型不能为空")
        private Club.ClubType type;
        
        @Size(max = 500, message = "社团宗旨长度不能超过500个字符")
        private String purpose;
        
        private String description;
        
        private String logo;
        
        private String coverImage;
        
        private String contactEmail;
        
        private String contactPhone;
        
        private String location;
    }
    
    /**
     * 更新社团请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        
        @Size(max = 100, message = "社团名称长度不能超过100个字符")
        private String name;
        
        private Club.ClubType type;
        
        @Size(max = 500, message = "社团宗旨长度不能超过500个字符")
        private String purpose;
        
        private String description;
        
        private String logo;
        
        private String coverImage;
        
        private String contactEmail;
        
        private String contactPhone;
        
        private String location;
        
        private String documentUrl;
    }
}
