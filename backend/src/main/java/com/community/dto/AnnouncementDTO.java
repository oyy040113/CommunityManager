package com.community.dto;

import com.community.entity.Announcement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
    
    private Long id;
    private String title;
    private String content;
    private Long clubId;
    private String clubName;
    private Long publisherId;
    private String publisherName;
    private Announcement.AnnouncementType type;
    private Boolean pinned;
    private Integer viewCount;
    private Boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    
    public static AnnouncementDTO fromEntity(Announcement announcement) {
        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .clubId(announcement.getClub().getId())
                .clubName(announcement.getClub().getName())
                .publisherId(announcement.getPublisher().getId())
                .publisherName(announcement.getPublisher().getRealName())
                .type(announcement.getType())
                .pinned(announcement.getPinned())
                .viewCount(announcement.getViewCount())
                .published(announcement.getPublished())
                .createdAt(announcement.getCreatedAt())
                .expireAt(announcement.getExpireAt())
                .build();
    }
    
    /**
     * 创建公告请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 200, message = "公告标题长度不能超过200个字符")
        private String title;
        
        @NotBlank(message = "公告内容不能为空")
        private String content;
        
        @NotNull(message = "社团ID不能为空")
        private Long clubId;
        
        private Announcement.AnnouncementType type = Announcement.AnnouncementType.NORMAL;
        
        private Boolean pinned = false;
        
        private LocalDateTime expireAt;
    }
    
    /**
     * 更新公告请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        
        @Size(max = 200, message = "公告标题长度不能超过200个字符")
        private String title;
        
        private String content;
        
        private Announcement.AnnouncementType type;
        
        private Boolean pinned;
        
        private Boolean published;
        
        private LocalDateTime expireAt;
    }
}
