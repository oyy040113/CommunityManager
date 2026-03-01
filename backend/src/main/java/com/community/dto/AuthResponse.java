package com.community.dto;

import com.community.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDTO user;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String username;
        private String realName;
        private String studentId;
        private String email;
        private String phone;
        private String avatar;
        private String department;
        private String major;
        private String grade;
        private User.UserRole role;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
        // 统计信息
        @Builder.Default
        private Integer clubCount = 0;
        @Builder.Default
        private Integer activityCount = 0;
        @Builder.Default
        private Integer feedbackCount = 0;
        
        public static UserDTO fromEntity(User user) {
            return UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .studentId(user.getStudentId())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .avatar(user.getAvatar())
                    .department(user.getDepartment())
                    .major(user.getMajor())
                    .grade(user.getGrade())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
        }
    }
}
