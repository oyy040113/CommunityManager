package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.dto.ClubDTO;
import com.community.dto.StatisticsDTO;
import com.community.entity.User;
import com.community.repository.ActivityRepository;
import com.community.repository.ActivityRegistrationRepository;
import com.community.repository.ClubMemberRepository;
import com.community.repository.ClubRepository;
import com.community.repository.UserRepository;
import com.community.service.AuthService;
import com.community.service.ClubService;
import com.community.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "数据统计", description = "系统数据统计相关接口")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    private final AuthService authService;
    private final ClubService clubService;
    private final ClubRepository clubRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;
    
    @GetMapping("/system")
    @Operation(summary = "系统统计", description = "获取系统整体统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StatisticsDTO.SystemStats>> getSystemStats() {
        StatisticsDTO.SystemStats stats = statisticsService.getSystemStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/clubs/{clubId}")
    @Operation(summary = "社团统计", description = "获取指定社团的统计数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.ClubStats>> getClubStats(@PathVariable Long clubId) {
        StatisticsDTO.ClubStats stats = statisticsService.getClubStats(clubId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/activities/{activityId}")
    @Operation(summary = "活动统计", description = "获取指定活动的统计数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.ActivityStats>> getActivityStats(@PathVariable Long activityId) {
        StatisticsDTO.ActivityStats stats = statisticsService.getActivityStats(activityId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @GetMapping("/my-growth")
    @Operation(summary = "我的成长轨迹", description = "获取当前用户的成长轨迹数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.UserGrowth>> getMyGrowth() {
        User currentUser = authService.getCurrentUser();
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(growth));
    }
    
    @GetMapping("/users/{userId}/growth")
    @Operation(summary = "用户成长轨迹", description = "获取指定用户的成长轨迹数据")
    public ResponseEntity<ApiResponse<StatisticsDTO.UserGrowth>> getUserGrowth(@PathVariable Long userId) {
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(userId);
        return ResponseEntity.ok(ApiResponse.success(growth));
    }
    
    @GetMapping("/overview")
    @Operation(summary = "统计概览", description = "获取当前用户管理的社团统计概览")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverviewStats() {
        User currentUser = authService.getCurrentUser();
        List<ClubDTO> myClubs = clubService.getClubsByLeader(currentUser.getId());
        
        int totalMembers = 0;
        int totalActivities = 0;
        double totalRating = 0;
        int ratingCount = 0;
        
        for (ClubDTO club : myClubs) {
            totalMembers += club.getMemberCount() != null ? club.getMemberCount() : 0;
            totalActivities += club.getActivityCount() != null ? club.getActivityCount() : 0;
            if (club.getActivityScore() != null && club.getActivityScore() > 0) {
                totalRating += club.getActivityScore();
                ratingCount++;
            }
        }
        
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalMembers", totalMembers);
        overview.put("totalClubs", myClubs.size());
        overview.put("totalActivities", totalActivities);
        overview.put("averageRating", ratingCount > 0 ? totalRating / ratingCount : 0);
        overview.put("clubs", myClubs);
        
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/public-overview")
    @Operation(summary = "首页公共统计", description = "获取首页展示的公开统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPublicOverviewStats() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("clubCount", clubRepository.countActiveClubs());
        overview.put("userCount", userRepository.count());
        overview.put("activityCount", activityRepository.countPublicActivities());
        overview.put("participationCount", registrationRepository.count());
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
    
    @GetMapping("/club-rankings")
    @Operation(summary = "社团排行", description = "获取社团活跃度排行榜")
    public ResponseEntity<ApiResponse<List<ClubDTO>>> getClubRankings(
            @RequestParam(defaultValue = "10") int limit) {
        List<ClubDTO> clubs = clubService.getTopActiveClubs(limit);
        return ResponseEntity.ok(ApiResponse.success(clubs));
    }

    @GetMapping("/clubs/{clubId}/member-growth")
    @Operation(summary = "成员增长趋势", description = "获取指定社团的成员增长趋势（近6个月）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMemberGrowthTrend(@PathVariable Long clubId) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            LocalDateTime start = month.atDay(1).atStartOfDay();
            LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
            long count = clubMemberRepository.findByClubId(clubId).stream()
                    .filter(m -> m.getJoinedAt() != null
                            && !m.getJoinedAt().isBefore(start) && !m.getJoinedAt().isAfter(end))
                    .count();
            Map<String, Object> row = new HashMap<>();
            row.put("month", month.toString());
            row.put("count", count);
            monthlyData.add(row);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("clubId", clubId);
        result.put("data", monthlyData);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/clubs/{clubId}/activity-participation")
    @Operation(summary = "活动参与统计", description = "获取指定社团近期活动的参与数据")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActivityParticipationStats(
            @PathVariable Long clubId) {
        List<Map<String, Object>> result = activityRepository
                .findCompletedActivitiesByClub(clubId, PageRequest.of(0, 10))
                .stream()
                .map(activity -> {
                    long registered = registrationRepository.countRegisteredByActivityId(activity.getId());
                    long checkedIn = registrationRepository.countCheckedInByActivityId(activity.getId());
                    Map<String, Object> row = new HashMap<>();
                    row.put("activityId", activity.getId());
                    row.put("title", activity.getTitle());
                    row.put("registered", registered);
                    row.put("checkedIn", checkedIn);
                    row.put("attendanceRate", registered > 0 ? (double) checkedIn / registered * 100 : 0);
                    return row;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/clubs/{clubId}/rating-distribution")
    @Operation(summary = "评分分布", description = "获取指定社团所有活动的评分分布")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRatingDistribution(@PathVariable Long clubId) {
        StatisticsDTO.ClubStats stats = statisticsService.getClubStats(clubId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        stats.getRecentActivities().forEach(a -> {
            if (a.getRatingDistribution() != null) {
                a.getRatingDistribution().forEach((k, v) -> distribution.merge(k, v, Long::sum));
            }
        });
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("clubId", clubId);
        resultMap.put("distribution", distribution);
        return ResponseEntity.ok(ApiResponse.success(resultMap));
    }

    @GetMapping("/popular-activities")
    @Operation(summary = "热门活动", description = "获取报名人数最多的活动列表")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularActivities(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> result = activityRepository
                .findAll(PageRequest.of(0, limit, org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "currentParticipants")))
                .stream()
                .map(activity -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", activity.getId());
                    row.put("title", activity.getTitle());
                    row.put("clubName", activity.getClub() != null ? activity.getClub().getName() : "");
                    row.put("currentParticipants", activity.getCurrentParticipants());
                    row.put("maxParticipants", activity.getMaxParticipants());
                    row.put("startTime", activity.getStartTime());
                    row.put("status", activity.getStatus());
                    return row;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/activities")
    @Operation(summary = "我的活动统计", description = "获取当前用户的活动参与统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserActivityStats() {
        User currentUser = authService.getCurrentUser();
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(currentUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("totalActivities", growth.getTotalActivitiesParticipated());
        result.put("attendedActivities", growth.getTotalActivitiesAttended());
        result.put("recentActivities", growth.getRecentActivities());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/clubs")
    @Operation(summary = "我的社团统计", description = "获取当前用户的社团参与统计")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserClubStats() {
        User currentUser = authService.getCurrentUser();
        StatisticsDTO.UserGrowth growth = statisticsService.getUserGrowth(currentUser.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("totalClubs", growth.getTotalClubs());
        result.put("contributionScore", growth.getTotalContributionScore());
        result.put("clubMemberships", growth.getClubMemberships());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
