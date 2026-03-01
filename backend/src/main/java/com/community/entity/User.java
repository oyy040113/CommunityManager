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
 * 用户实体类
 * 存储系统用户的基本信息
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 50)
    private String realName;
    
    @Column(unique = true, length = 50)
    private String studentId; // 学号
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 255)
    private String avatar; // 头像URL
    
    @Column(length = 50)
    private String department; // 院系
    
    @Column(length = 50)
    private String major; // 专业
    
    @Column(length = 10)
    private String grade; // 年级
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // 用户角色: ADMIN, CLUB_LEADER, MEMBER
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime lastLoginAt;
    
    // 用户加入的社团成员关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ClubMember> clubMemberships = new HashSet<>();
    
    public enum UserRole {
        ADMIN,        // 超级管理员 - 校社团管理部门，审批社团/活动、用户管理、全局管控
        CLUB_LEADER,  // 社团管理员/负责人 - 管理社团信息、成员、活动等
        TEACHER,      // 指导老师 - 审核活动方案、查看所指导社团动态
        USER          // 普通学生 - 浏览社团/活动、报名参与
    }
}
