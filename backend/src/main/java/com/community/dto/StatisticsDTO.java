package com.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统计数据DTO
 */
public class StatisticsDTO {
    
    /**
     * 系统整体统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemStats {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalClubs;
        private Long activeClubs;
        private Long totalActivities;
        private Long completedActivities;
        private Long monthlyActivities;
        private Map<String, Long> clubsByType;
        private Map<String, Long> activitiesByMonth;
    }
    
    /**
     * 社团统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClubStats {
        private Long clubId;
        private String clubName;
        private Integer totalMembers;
        private Integer newMembersThisMonth;
        private Integer totalActivities;
        private Integer completedActivities;
        private Double averageActivityRating;
        private Double activityScore;
        private Map<String, Long> membersByRole;
        private List<ActivityStats> recentActivities;
    }
    
    /**
     * 活动统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityStats {
        private Long activityId;
        private String activityTitle;
        private Integer registeredCount;
        private Integer attendedCount;
        private Integer absentCount;
        private Double attendanceRate;
        private Double averageRating;
        private Integer feedbackCount;
        private Map<Integer, Long> ratingDistribution;
    }
    
    /**
     * 用户成长轨迹
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserGrowth {
        private Long userId;
        private String userName;
        private Integer totalClubs;
        private Integer totalActivitiesParticipated;
        private Integer totalActivitiesAttended;
        private Double totalContributionScore;
        private List<ClubMembershipSummary> clubMemberships;
        private List<ActivityParticipationSummary> recentActivities;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClubMembershipSummary {
        private Long clubId;
        private String clubName;
        private String role;
        private String position;
        private Integer activityParticipation;
        private Double contributionScore;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityParticipationSummary {
        private Long activityId;
        private String activityTitle;
        private String clubName;
        private String status;
        private Boolean checkedIn;
        private Integer rating;
    }
}
