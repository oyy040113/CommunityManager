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
 * 公告实体类
 * 存储社团公告信息
 */
@Entity
@Table(name = "announcements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Announcement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title; // 公告标题
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 公告内容
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club; // 所属社团
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher; // 发布者
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnouncementType type = AnnouncementType.NORMAL;
    
    @Column(nullable = false)
    private Boolean pinned = false; // 是否置顶
    
    @Column(nullable = false)
    private Integer viewCount = 0; // 浏览次数
    
    @Column(nullable = false)
    private Boolean published = true; // 是否发布
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime expireAt; // 过期时间
    
    public enum AnnouncementType {
        NORMAL,     // 普通公告
        IMPORTANT,  // 重要公告
        URGENT,     // 紧急公告
        ACTIVITY    // 活动通知
    }
}
