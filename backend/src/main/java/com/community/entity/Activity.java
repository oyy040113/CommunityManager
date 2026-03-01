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
 * 活动实体类
 * 存储社团活动的详细信息
 */
@Entity
@Table(name = "activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title; // 活动标题
    
    @Column(columnDefinition = "TEXT")
    private String description; // 活动描述
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club; // 所属社团
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer; // 活动组织者
    
    @Column(nullable = false)
    private LocalDateTime startTime; // 开始时间
    
    @Column(nullable = false)
    private LocalDateTime endTime; // 结束时间
    
    @Column(length = 255)
    private String location; // 活动地点
    
    @Column(length = 255)
    private String coverImage; // 封面图片
    
    @Column(nullable = false)
    @Builder.Default
    private Integer maxParticipants = 0; // 最大参与人数，0表示不限
    
    @Column(nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0; // 当前报名人数
    
    @Column(nullable = false)
    private LocalDateTime registrationDeadline; // 报名截止时间
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ActivityStatus status = ActivityStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING; // 审批状态
    
    @Column(columnDefinition = "TEXT")
    private String rejectReason; // 拒绝原因
    
    private LocalDateTime approvedAt; // 审批通过时间
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // 审批人
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CheckInType checkInType = CheckInType.QR_CODE; // 签到方式
    
    @Column(length = 255)
    private String qrCodeUrl; // 签到二维码URL
    
    @Column(columnDefinition = "TEXT")
    private String customForm; // 自定义报名表单(JSON格式)
    
    @Column(columnDefinition = "TEXT")
    private String summary; // 活动总结
    
    @Column(columnDefinition = "TEXT")
    private String summaryImages; // 活动总结图片(JSON数组)
    
    @Column(nullable = false)
    @Builder.Default
    private Double rating = 0.0; // 活动评分
    
    @Column(nullable = false)
    @Builder.Default
    private Integer ratingCount = 0; // 评分人数
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // 活动报名记录
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ActivityRegistration> registrations = new HashSet<>();
    
    // 活动反馈
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ActivityFeedback> feedbacks = new HashSet<>();
    
    public enum ActivityStatus {
        DRAFT,          // 草稿
        PUBLISHED,      // 已发布(报名中)
        REGISTRATION_CLOSED, // 报名截止
        ONGOING,        // 进行中
        COMPLETED,      // 已完成
        CANCELLED       // 已取消
    }
    
    public enum ApprovalStatus {
        PENDING,        // 待审批
        APPROVED,       // 已通过
        REJECTED        // 已拒绝
    }
    
    public enum CheckInType {
        QR_CODE,        // 二维码签到
        MANUAL,         // 手动签到
        LOCATION,       // 位置签到
        NONE            // 无需签到
    }
}
