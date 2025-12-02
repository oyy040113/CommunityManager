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
 * 社团成员关系实体类
 * 管理用户与社团的多对多关系
 */
@Entity
@Table(name = "club_members", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "club_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ClubMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.MEMBER; // 成员角色
    
    @Column(length = 100)
    private String position; // 职位名称，如：活动策划、财务管理员
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.PENDING; // 成员状态
    
    @Column(columnDefinition = "TEXT")
    private String applicationReason; // 加入申请理由
    
    @Column(columnDefinition = "TEXT")
    private String rejectReason; // 拒绝理由
    
    @Column(nullable = false)
    private Integer activityParticipation = 0; // 参与活动次数
    
    @Column(nullable = false)
    private Double contributionScore = 0.0; // 贡献度评分
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt; // 加入时间/申请时间
    
    private LocalDateTime approvedAt; // 审核通过时间
    
    public enum MemberRole {
        LEADER,     // 社团负责人
        VICE_LEADER, // 副社长
        ADMIN,      // 管理员
        PLANNER,    // 活动策划
        FINANCE,    // 财务管理员
        PUBLICITY,  // 宣传
        MEMBER      // 普通成员
    }
    
    public enum MemberStatus {
        PENDING,    // 待审核
        APPROVED,   // 已通过
        REJECTED,   // 已拒绝
        WITHDRAWN,  // 已退出
        REMOVED     // 被移除
    }
}
