package com.community.dto;

import com.community.entity.Activity;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    
    private Long id;
    private String title;
    private String description;
    private Long clubId;
    private String clubName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String coverImage;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime registrationDeadline;
    private Activity.ActivityStatus status;
    private Activity.CheckInType checkInType;
    private String qrCodeUrl;
    private String customForm;
    private String summary;
    private String summaryImages;
    private Double rating;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private Boolean isRegistered; // 当前用户是否已报名
    private Boolean isCheckedIn; // 当前用户是否已签到
    
    public static ActivityDTO fromEntity(Activity activity) {
        return ActivityDTO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .clubId(activity.getClub().getId())
                .clubName(activity.getClub().getName())
                .organizerId(activity.getOrganizer() != null ? activity.getOrganizer().getId() : null)
                .organizerName(activity.getOrganizer() != null ? activity.getOrganizer().getRealName() : null)
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .location(activity.getLocation())
                .coverImage(activity.getCoverImage())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(activity.getCurrentParticipants())
                .registrationDeadline(activity.getRegistrationDeadline())
                .status(activity.getStatus())
                .checkInType(activity.getCheckInType())
                .qrCodeUrl(activity.getQrCodeUrl())
                .customForm(activity.getCustomForm())
                .summary(activity.getSummary())
                .summaryImages(activity.getSummaryImages())
                .rating(activity.getRating())
                .ratingCount(activity.getRatingCount())
                .createdAt(activity.getCreatedAt())
                .build();
    }
    
    /**
     * 创建活动请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        
        @NotBlank(message = "活动标题不能为空")
        @Size(max = 200, message = "活动标题长度不能超过200个字符")
        private String title;
        
        private String description;
        
        @NotNull(message = "社团ID不能为空")
        private Long clubId;
        
        @NotNull(message = "开始时间不能为空")
        @Future(message = "开始时间必须是将来的时间")
        private LocalDateTime startTime;
        
        @NotNull(message = "结束时间不能为空")
        @Future(message = "结束时间必须是将来的时间")
        private LocalDateTime endTime;
        
        @NotBlank(message = "活动地点不能为空")
        private String location;
        
        private String coverImage;
        
        private Integer maxParticipants = 0;
        
        @NotNull(message = "报名截止时间不能为空")
        @Future(message = "报名截止时间必须是将来的时间")
        private LocalDateTime registrationDeadline;
        
        private Activity.CheckInType checkInType = Activity.CheckInType.QR_CODE;
        
        private String customForm;
    }
    
    /**
     * 更新活动请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        
        @Size(max = 200, message = "活动标题长度不能超过200个字符")
        private String title;
        
        private String description;
        
        private LocalDateTime startTime;
        
        private LocalDateTime endTime;
        
        private String location;
        
        private String coverImage;
        
        private Integer maxParticipants;
        
        private LocalDateTime registrationDeadline;
        
        private Activity.CheckInType checkInType;
        
        private String customForm;
        
        private Activity.ActivityStatus status;
    }
    
    /**
     * 活动总结请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryRequest {
        
        @NotBlank(message = "活动总结不能为空")
        private String summary;
        
        private String summaryImages;
    }
}
