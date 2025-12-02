package com.community.service;

import com.community.dto.StatisticsDTO;
import com.community.entity.Activity;
import com.community.entity.Club;
import com.community.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ActivityFeedbackRepository feedbackRepository;
    
    /**
     * 获取系统整体统计
     */
    public StatisticsDTO.SystemStats getSystemStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long totalClubs = clubRepository.count();
        long activeClubs = clubRepository.countActiveClubs();
        long totalActivities = activityRepository.count();
        
        // 本月活动数
        LocalDateTime monthStart = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
        long monthlyActivities = activityRepository.countCompletedActivitiesBetweenDates(monthStart, monthEnd);
        
        // 按类型统计社团
        Map<String, Long> clubsByType = new HashMap<>();
        List<Object[]> typeStats = clubRepository.countClubsByType();
        for (Object[] row : typeStats) {
            clubsByType.put(((Club.ClubType) row[0]).name(), (Long) row[1]);
        }
        
        // 按月统计活动 (简化：只返回当月)
        Map<String, Long> activitiesByMonth = new HashMap<>();
        activitiesByMonth.put(LocalDateTime.now().getMonth().name(), monthlyActivities);
        
        return StatisticsDTO.SystemStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalClubs(totalClubs)
                .activeClubs(activeClubs)
                .totalActivities(totalActivities)
                .completedActivities(activityRepository.findByClubIdAndStatus(null, Activity.ActivityStatus.COMPLETED, PageRequest.of(0, 1)).getTotalElements())
                .monthlyActivities(monthlyActivities)
                .clubsByType(clubsByType)
                .activitiesByMonth(activitiesByMonth)
                .build();
    }
    
    /**
     * 获取社团统计
     */
    public StatisticsDTO.ClubStats getClubStats(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("社团不存在"));
        
        long totalMembers = clubMemberRepository.countApprovedMembers(clubId);
        long completedActivities = activityRepository.countCompletedActivitiesByClub(clubId);
        
        // 按角色统计成员
        Map<String, Long> membersByRole = new HashMap<>();
        List<Object[]> roleStats = clubMemberRepository.countMembersByRole(clubId);
        for (Object[] row : roleStats) {
            membersByRole.put(row[0].toString(), (Long) row[1]);
        }
        
        // 最近活动统计
        List<Activity> recentActivities = activityRepository.findCompletedActivitiesByClub(
                clubId, PageRequest.of(0, 5));
        List<StatisticsDTO.ActivityStats> activityStats = recentActivities.stream()
                .map(this::buildActivityStats)
                .collect(Collectors.toList());
        
        return StatisticsDTO.ClubStats.builder()
                .clubId(clubId)
                .clubName(club.getName())
                .totalMembers((int) totalMembers)
                .totalActivities(club.getActivityCount())
                .completedActivities((int) completedActivities)
                .activityScore(club.getActivityScore())
                .membersByRole(membersByRole)
                .recentActivities(activityStats)
                .build();
    }
    
    /**
     * 获取活动统计
     */
    public StatisticsDTO.ActivityStats getActivityStats(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        return buildActivityStats(activity);
    }
    
    /**
     * 获取用户成长轨迹
     */
    public StatisticsDTO.UserGrowth getUserGrowth(Long userId) {
        // 获取用户加入的社团
        List<StatisticsDTO.ClubMembershipSummary> memberships = clubMemberRepository
                .findApprovedMembershipsByUserId(userId)
                .stream()
                .map(member -> StatisticsDTO.ClubMembershipSummary.builder()
                        .clubId(member.getClub().getId())
                        .clubName(member.getClub().getName())
                        .role(member.getRole().name())
                        .position(member.getPosition())
                        .activityParticipation(member.getActivityParticipation())
                        .contributionScore(member.getContributionScore())
                        .build())
                .collect(Collectors.toList());
        
        // 获取最近参与的活动
        List<StatisticsDTO.ActivityParticipationSummary> activities = registrationRepository
                .findUserActivityHistory(userId, PageRequest.of(0, 10))
                .stream()
                .map(reg -> StatisticsDTO.ActivityParticipationSummary.builder()
                        .activityId(reg.getActivity().getId())
                        .activityTitle(reg.getActivity().getTitle())
                        .clubName(reg.getActivity().getClub().getName())
                        .status(reg.getStatus().name())
                        .checkedIn(reg.getCheckedIn())
                        .build())
                .collect(Collectors.toList());
        
        // 计算统计数据
        int totalClubs = memberships.size();
        int totalActivities = activities.size();
        int attendedActivities = (int) activities.stream().filter(a -> a.getCheckedIn()).count();
        double totalContribution = memberships.stream()
                .mapToDouble(m -> m.getContributionScore() != null ? m.getContributionScore() : 0)
                .sum();
        
        return StatisticsDTO.UserGrowth.builder()
                .userId(userId)
                .totalClubs(totalClubs)
                .totalActivitiesParticipated(totalActivities)
                .totalActivitiesAttended(attendedActivities)
                .totalContributionScore(totalContribution)
                .clubMemberships(memberships)
                .recentActivities(activities)
                .build();
    }
    
    /**
     * 构建活动统计
     */
    private StatisticsDTO.ActivityStats buildActivityStats(Activity activity) {
        long registered = registrationRepository.countRegisteredByActivityId(activity.getId());
        long checkedIn = registrationRepository.countCheckedInByActivityId(activity.getId());
        Double avgRating = feedbackRepository.getAverageRatingByActivityId(activity.getId());
        long feedbackCount = feedbackRepository.countByActivityId(activity.getId());
        
        // 评分分布
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        List<Object[]> ratingStats = feedbackRepository.getRatingDistributionByActivityId(activity.getId());
        for (Object[] row : ratingStats) {
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }
        
        return StatisticsDTO.ActivityStats.builder()
                .activityId(activity.getId())
                .activityTitle(activity.getTitle())
                .registeredCount((int) registered)
                .attendedCount((int) checkedIn)
                .absentCount((int) (registered - checkedIn))
                .attendanceRate(registered > 0 ? (double) checkedIn / registered * 100 : 0)
                .averageRating(avgRating != null ? avgRating : 0)
                .feedbackCount((int) feedbackCount)
                .ratingDistribution(ratingDistribution)
                .build();
    }
}
