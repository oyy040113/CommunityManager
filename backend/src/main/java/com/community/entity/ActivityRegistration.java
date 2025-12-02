package com.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 活动报名实体类
 * 记录用户报名活动的信息
 */
@Entity
@Table(name = "activity_registrations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "activity_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ActivityRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;
    
    @Column(columnDefinition = "TEXT")
    private String formData; // 自定义表单填写数据(JSON格式)
    
    @Column(nullable = false)
    private Boolean checkedIn = false; // 是否已签到
    
    private LocalDateTime checkInTime; // 签到时间
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt; // 报名时间
    
    private LocalDateTime cancelledAt; // 取消时间
    
    public enum RegistrationStatus {
        REGISTERED,  // 已报名
        CANCELLED,   // 已取消
        ATTENDED,    // 已参加
        ABSENT       // 缺席
    }
}
