package com.community.dto;

import com.community.entity.ActivityRegistration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活动报名DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRegistrationDTO {
    
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String studentId;
    private String avatar;
    private Long activityId;
    private String activityTitle;
    private ActivityRegistration.RegistrationStatus status;
    private String formData;
    private Boolean checkedIn;
    private LocalDateTime checkInTime;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    
    public static ActivityRegistrationDTO fromEntity(ActivityRegistration registration) {
        return ActivityRegistrationDTO.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .username(registration.getUser().getUsername())
                .realName(registration.getUser().getRealName())
                .studentId(registration.getUser().getStudentId())
                .avatar(registration.getUser().getAvatar())
                .activityId(registration.getActivity().getId())
                .activityTitle(registration.getActivity().getTitle())
                .status(registration.getStatus())
                .formData(registration.getFormData())
                .checkedIn(registration.getCheckedIn())
                .checkInTime(registration.getCheckInTime())
                .registeredAt(registration.getRegisteredAt())
                .cancelledAt(registration.getCancelledAt())
                .build();
    }
    
    /**
     * 报名请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String formData; // 自定义表单数据(JSON格式)
    }
    
    /**
     * 签到请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckInRequest {
        private String qrCode; // 签到二维码
    }
}
