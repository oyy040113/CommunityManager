package com.community.dto;

import com.community.entity.ActivityFeedback;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动反馈DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityFeedbackDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private Long activityId;
    private String activityTitle;
    private Integer rating;
    private String comment;
    private String images;
    private Boolean anonymous;
    private LocalDateTime createdAt;
    
    public static ActivityFeedbackDTO fromEntity(ActivityFeedback feedback) {
        ActivityFeedbackDTOBuilder builder = ActivityFeedbackDTO.builder()
                .id(feedback.getId())
                .activityId(feedback.getActivity().getId())
                .activityTitle(feedback.getActivity().getTitle())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .images(feedback.getImages())
                .anonymous(feedback.getAnonymous())
                .createdAt(feedback.getCreatedAt());
        
        if (!feedback.getAnonymous()) {
            builder.userId(feedback.getUser().getId())
                   .username(feedback.getUser().getUsername())
                   .realName(feedback.getUser().getRealName())
                   .avatar(feedback.getUser().getAvatar());
        }
        
        return builder.build();
    }
    
    /**
     * 创建反馈请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        
        @NotNull(message = "活动ID不能为空")
        private Long activityId;
        
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        private Integer rating;
        
        private String comment;
        
        private String images;
        
        private Boolean anonymous = false;
    }
}
