package com.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 社团实体类
 * 存储社团的基本信息
 */
@Entity
@Table(name = "clubs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Club {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String name; // 社团名称
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubType type; // 社团类型
    
    @Column(length = 500)
    private String purpose; // 社团宗旨
    
    @Column(columnDefinition = "TEXT")
    private String description; // 社团简介
    
    @Column(length = 255)
    private String logo; // 社团Logo URL
    
    @Column(length = 255)
    private String coverImage; // 封面图片
    
    @Column(length = 100)
    private String contactEmail;
    
    @Column(length = 20)
    private String contactPhone;
    
    @Column(length = 255)
    private String location; // 活动地点
    
    @Column(length = 255)
    private String documentUrl; // 社团简介文档URL
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader; // 社团负责人
    
    @Column(nullable = false)
    private Integer memberCount = 0; // 成员数量
    
    @Column(nullable = false)
    private Integer activityCount = 0; // 活动数量
    
    @Column(nullable = false)
    private Double activityScore = 0.0; // 活跃度评分
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubStatus status = ClubStatus.PENDING; // 默认待审批
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator; // 社团创建者（申请人）
    
    @Column(columnDefinition = "TEXT")
    private String rejectReason; // 拒绝原因
    
    private LocalDateTime approvedAt; // 审批通过时间
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // 审批人
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 社团成员
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ClubMember> members = new HashSet<>();
    
    // 社团活动
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Activity> activities = new HashSet<>();
    
    // 社团公告
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Announcement> announcements = new HashSet<>();
    
    public enum ClubType {
        ACADEMIC,    // 学术科技类
        CULTURAL,    // 文化艺术类
        SPORTS,      // 体育运动类
        VOLUNTEER,   // 志愿服务类
        INNOVATION,  // 创新创业类
        INTEREST,    // 兴趣爱好类
        OTHER        // 其他类型
    }
    
    public enum ClubStatus {
        PENDING,     // 待审批
        ACTIVE,      // 活跃
        INACTIVE,    // 不活跃
        REJECTED,    // 已拒绝
        SUSPENDED,   // 暂停
        DISSOLVED    // 解散
    }
}
