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
 * 消息通知实体类
 * 存储用户的通知消息
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 接收用户
    
    @Column(nullable = false, length = 200)
    private String title; // 通知标题
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 通知内容
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private Boolean read = false; // 是否已读
    
    private Long relatedId; // 关联ID(如活动ID、社团ID等)
    
    @Column(length = 50)
    private String relatedType; // 关联类型
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt; // 阅读时间
    
    public enum NotificationType {
        SYSTEM,         // 系统通知
        CLUB_JOIN,      // 社团加入
        CLUB_APPROVED,  // 申请通过
        CLUB_REJECTED,  // 申请拒绝
        ACTIVITY_REMINDER, // 活动提醒
        ACTIVITY_UPDATE,   // 活动更新
        ANNOUNCEMENT       // 公告通知
    }
}
