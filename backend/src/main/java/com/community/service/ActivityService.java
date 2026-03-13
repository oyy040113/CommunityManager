package com.community.service;

import com.community.dto.ActivityDTO;
import com.community.dto.ActivityFeedbackDTO;
import com.community.dto.ActivityRegistrationDTO;
import com.community.entity.*;
import com.community.exception.BusinessException;
import com.community.repository.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 活动服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ActivityFeedbackRepository feedbackRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final NotificationService notificationService;
    
    /**
     * 将Activity实体转换为DTO并填充签到人数
     */
    private ActivityDTO enrichDTO(Activity activity) {
        ActivityDTO dto = ActivityDTO.fromEntity(activity);
        dto.setCheckedInCount((int) registrationRepository.countCheckedInByActivityId(activity.getId()));
        return dto;
    }
    
    /**
     * 创建活动（需要审批）
     */
    @Transactional
    public ActivityDTO createActivity(ActivityDTO.CreateRequest request, User organizer) {
        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new BusinessException("社团不存在"));
        
        // 检查社团状态
        if (club.getStatus() != Club.ClubStatus.ACTIVE) {
            throw new BusinessException("该社团未激活，无法创建活动");
        }
        
        // 检查权限
        checkActivityManagePermission(club, organizer);
        
        // 验证时间
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        if (request.getRegistrationDeadline().isAfter(request.getEndTime())) {
            throw new BusinessException("报名截止时间不能晚于活动结束时间");
        }
        
        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .club(club)
                .organizer(organizer)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .coverImage(request.getCoverImage())
            .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 0)
                .currentParticipants(0)
                .registrationDeadline(request.getRegistrationDeadline())
                .status(Activity.ActivityStatus.DRAFT)
                .approvalStatus(Activity.ApprovalStatus.PENDING) // 需要审批
            .checkInType(request.getCheckInType() != null ? request.getCheckInType() : Activity.CheckInType.QR_CODE)
                .customForm(request.getCustomForm())
                .build();
        
        activity = activityRepository.save(activity);
        
        // 更新社团活动数
        club.setActivityCount(club.getActivityCount() + 1);
        clubRepository.save(club);
        
        log.info("活动创建成功（待审批）: {} by {}", activity.getTitle(), organizer.getUsername());
        
        return enrichDTO(activity);
    }
    
    /**
     * 审批活动（管理员）
     */
    @Transactional
    public ActivityDTO approveActivity(Long activityId, ActivityDTO.ApprovalRequest request, User admin) {
        Activity activity = getActivityEntity(activityId);
        
        if (activity.getApprovalStatus() != Activity.ApprovalStatus.PENDING) {
            throw new BusinessException("该活动不在待审批状态");
        }
        
        if (request.getApproved()) {
            activity.setApprovalStatus(Activity.ApprovalStatus.APPROVED);
            activity.setApprovedBy(admin);
            activity.setApprovedAt(LocalDateTime.now());
            
            // 审批通过后自动发布活动
            if (activity.getStatus() == Activity.ActivityStatus.DRAFT) {
                LocalDateTime now = LocalDateTime.now();
                
                // 根据当前时间和活动时间判断状态
                if (now.isAfter(activity.getEndTime())) {
                    activity.setStatus(Activity.ActivityStatus.COMPLETED);
                    log.info("活动审批通过，但活动已结束: {}", activity.getTitle());
                } else if (now.isAfter(activity.getStartTime())) {
                    activity.setStatus(Activity.ActivityStatus.ONGOING);
                    log.info("活动审批通过，活动已开始: {}", activity.getTitle());
                } else {
                    activity.setStatus(Activity.ActivityStatus.PUBLISHED);
                    log.info("活动审批通过并自动发布: {}", activity.getTitle());
                }
                
                // 生成签到二维码
                if (activity.getCheckInType() == Activity.CheckInType.QR_CODE) {
                    String qrCode = generateQRCode(activity.getId());
                    activity.setQrCodeUrl(qrCode);
                }
            }
            
            // 通知组织者
            notificationService.sendActivityApprovedNotification(activity.getOrganizer(), activity);
            
            // 通知社团成员
            notificationService.sendActivityPublishedNotification(activity);
        } else {
            activity.setApprovalStatus(Activity.ApprovalStatus.REJECTED);
            activity.setRejectReason(request.getRejectReason());
            
            // 通知组织者
            notificationService.sendActivityRejectedNotification(activity.getOrganizer(), activity, request.getRejectReason());
            
            log.info("活动审批拒绝: {}", activity.getTitle());
        }
        
        activity = activityRepository.save(activity);
        return enrichDTO(activity);
    }
    
    /**
     * 获取待审批活动列表（管理员）
     */
    public Page<ActivityDTO> getPendingActivities(Pageable pageable) {
        return activityRepository.findPendingActivities(pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 管理员搜索活动（支持所有状态）
     */
    public Page<ActivityDTO> searchActivitiesAdmin(String keyword, Long clubId, 
                                                    Activity.ActivityStatus status,
                                                    Activity.ApprovalStatus approvalStatus, 
                                                    Pageable pageable) {
        return activityRepository.searchActivitiesAdmin(keyword, clubId, status, approvalStatus, pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 统计待审批活动数
     */
    public long countPendingActivities() {
        return activityRepository.countPendingActivities();
    }
    
    /**
     * 更新活动
     */
    @Transactional
    public ActivityDTO updateActivity(Long activityId, ActivityDTO.UpdateRequest request, User operator) {
        Activity activity = getActivityEntity(activityId);
        
        // 检查权限
        checkActivityManagePermission(activity.getClub(), operator);
        
        if (request.getTitle() != null) {
            activity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            activity.setDescription(request.getDescription());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getLocation() != null) {
            activity.setLocation(request.getLocation());
        }
        if (request.getCoverImage() != null) {
            activity.setCoverImage(request.getCoverImage());
        }
        if (request.getMaxParticipants() != null) {
            activity.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getRegistrationDeadline() != null) {
            activity.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getCheckInType() != null) {
            activity.setCheckInType(request.getCheckInType());
        }
        if (request.getCustomForm() != null) {
            activity.setCustomForm(request.getCustomForm());
        }
        if (request.getStatus() != null) {
            activity.setStatus(request.getStatus());
        }
        
        // 验证更新后的时间
        if (activity.getEndTime().isBefore(activity.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        if (activity.getRegistrationDeadline().isAfter(activity.getEndTime())) {
            throw new BusinessException("报名截止时间不能晚于活动结束时间");
        }
        
        // 编辑后重新检查活动状态
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStatus() == Activity.ActivityStatus.PUBLISHED) {
            // 如果活动已发布，检查是否应该变为进行中或已完成
            if (now.isAfter(activity.getEndTime())) {
                activity.setStatus(Activity.ActivityStatus.COMPLETED);
                log.info("活动已结束，自动更新为已完成状态: {}", activity.getTitle());
            } else if (now.isAfter(activity.getStartTime())) {
                activity.setStatus(Activity.ActivityStatus.ONGOING);
                log.info("活动已开始，自动更新为进行中状态: {}", activity.getTitle());
            }
        } else if (activity.getStatus() == Activity.ActivityStatus.ONGOING) {
            // 如果活动进行中，检查是否已结束
            if (now.isAfter(activity.getEndTime())) {
                activity.setStatus(Activity.ActivityStatus.COMPLETED);
                log.info("活动已结束，自动更新为已完成状态: {}", activity.getTitle());
            }
        }
        
        activity = activityRepository.save(activity);
        log.info("活动更新: {}", activity.getTitle());
        
        return enrichDTO(activity);
    }
    
    /**
     * 发布活动
     */
    @Transactional
    public ActivityDTO publishActivity(Long activityId, User operator) {
        Activity activity = getActivityEntity(activityId);
        checkActivityManagePermission(activity.getClub(), operator);
        
        // 如果已经发布，直接返回
        if (activity.getStatus() == Activity.ActivityStatus.PUBLISHED) {
            log.info("活动已经是发布状态: {}", activity.getTitle());
            return enrichDTO(activity);
        }
        
        if (activity.getStatus() != Activity.ActivityStatus.DRAFT) {
            throw new BusinessException("只有草稿状态的活动可以发布");
        }
        
        // 检查审批状态
        if (activity.getApprovalStatus() != Activity.ApprovalStatus.APPROVED) {
            throw new BusinessException("活动未通过审批，无法发布");
        }
        
        activity.setStatus(Activity.ActivityStatus.PUBLISHED);
        
        // 生成签到二维码
        if (activity.getCheckInType() == Activity.CheckInType.QR_CODE && activity.getQrCodeUrl() == null) {
            String qrCode = generateQRCode(activity.getId());
            activity.setQrCodeUrl(qrCode);
        }
        
        activity = activityRepository.save(activity);
        
        // 通知社团成员
        notificationService.sendActivityPublishedNotification(activity);
        
        log.info("活动发布: {}", activity.getTitle());
        
        return enrichDTO(activity);
    }
    
    /**
     * 删除活动
     */
    @Transactional
    public void deleteActivity(Long activityId, User operator) {
        Activity activity = getActivityEntity(activityId);
        Club club = activity.getClub();
        
        // 检查权限
        checkActivityManagePermission(club, operator);
        
        // 检查活动状态，进行中的活动不能删除
        if (activity.getStatus() == Activity.ActivityStatus.ONGOING) {
            throw new BusinessException("进行中的活动不能删除");
        }
        
        // 删除相关的报名记录
        registrationRepository.deleteByActivityId(activityId);
        
        // 删除相关的反馈记录
        feedbackRepository.deleteByActivityId(activityId);
        
        // 删除活动
        activityRepository.delete(activity);
        
        // 更新社团活动数
        club.setActivityCount(Math.max(0, club.getActivityCount() - 1));
        clubRepository.save(club);
        
        log.info("活动删除成功: {} by {}", activity.getTitle(), operator.getUsername());
    }
    
    /**
     * 获取活动详情
     */
    @Transactional
    public ActivityDTO getActivityById(Long activityId, User currentUser) {
        Activity activity = getActivityEntity(activityId);
        
        // 自动更新活动状态（如果已批准）
        if (activity.getApprovalStatus() == Activity.ApprovalStatus.APPROVED) {
            LocalDateTime now = LocalDateTime.now();
            boolean statusChanged = false;
            
            if (activity.getStatus() == Activity.ActivityStatus.PUBLISHED) {
                // 如果活动已发布，检查是否应该变为进行中或已完成
                if (now.isAfter(activity.getEndTime())) {
                    activity.setStatus(Activity.ActivityStatus.COMPLETED);
                    statusChanged = true;
                    log.info("活动状态自动更新为已完成: {}", activity.getTitle());
                } else if (now.isAfter(activity.getStartTime())) {
                    activity.setStatus(Activity.ActivityStatus.ONGOING);
                    statusChanged = true;
                    log.info("活动状态自动更新为进行中: {}", activity.getTitle());
                }
            } else if (activity.getStatus() == Activity.ActivityStatus.ONGOING) {
                // 如果活动进行中，检查是否已结束
                if (now.isAfter(activity.getEndTime())) {
                    activity.setStatus(Activity.ActivityStatus.COMPLETED);
                    statusChanged = true;
                    log.info("活动状态自动更新为已完成: {}", activity.getTitle());
                }
            }
            
            if (statusChanged) {
                activity = activityRepository.save(activity);
            }
        }
        
        ActivityDTO dto = enrichDTO(activity);
        
        // 检查当前用户是否已报名
        if (currentUser != null) {
            registrationRepository.findByUserIdAndActivityId(currentUser.getId(), activityId)
                    .ifPresent(reg -> {
                        dto.setIsRegistered(reg.getStatus() == ActivityRegistration.RegistrationStatus.REGISTERED 
                                || reg.getStatus() == ActivityRegistration.RegistrationStatus.ATTENDED);
                        dto.setIsCheckedIn(reg.getCheckedIn());
                    });
        }
        
        return dto;
    }
    
    /**
     * 获取活动实体
     */
    public Activity getActivityEntity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
    }
    
    /**
     * 搜索活动
     */
    public Page<ActivityDTO> searchActivities(String keyword, Long clubId, 
                                               Activity.ActivityStatus status, Pageable pageable) {
        return activityRepository.searchActivities(keyword, clubId, status, pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 获取即将开始的活动
     */
    public Page<ActivityDTO> getUpcomingActivities(Pageable pageable) {
        return activityRepository.findUpcomingActivities(LocalDateTime.now(), pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 获取社团活动列表
     */
    public Page<ActivityDTO> getClubActivities(Long clubId, Pageable pageable) {
        return activityRepository.findByClubId(clubId, pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 报名活动
     */
    @Transactional
    public ActivityRegistrationDTO registerActivity(Long activityId, 
                                                     ActivityRegistrationDTO.RegisterRequest request, 
                                                     User user) {
        Activity activity = getActivityEntity(activityId);
        LocalDateTime now = LocalDateTime.now();

        // 先按当前时间归一化状态，避免旧状态导致误判（如仍为DRAFT/REGISTRATION_CLOSED）
        activity = refreshActivityStatusForRegistration(activity, now);

        if (activity.getApprovalStatus() != Activity.ApprovalStatus.APPROVED) {
            throw new BusinessException("活动未通过审批，暂不可报名");
        }

        if (activity.getStatus() == Activity.ActivityStatus.CANCELLED) {
            throw new BusinessException("活动已取消");
        }

        if (now.isAfter(activity.getRegistrationDeadline())) {
            throw new BusinessException("报名已截止");
        }
        
        // 检查活动状态
        if (activity.getStatus() != Activity.ActivityStatus.PUBLISHED) {
            throw new BusinessException("活动未开放报名");
        }
        if (activity.getMaxParticipants() > 0 && 
            activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException("报名人数已满");
        }
        
        // 检查是否已报名
        if (registrationRepository.existsByUserIdAndActivityId(user.getId(), activityId)) {
            throw new BusinessException("您已报名该活动");
        }
        
        ActivityRegistration registration = ActivityRegistration.builder()
                .user(user)
                .activity(activity)
                .status(ActivityRegistration.RegistrationStatus.REGISTERED)
                .formData(request != null ? request.getFormData() : null)
                .checkedIn(false)
                .build();
        
        registration = registrationRepository.save(registration);
        
        // 更新报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityRepository.save(activity);
        
        log.info("用户报名活动: {} -> {}", user.getUsername(), activity.getTitle());
        
        return ActivityRegistrationDTO.fromEntity(registration);
    }

    /**
     * 报名前根据时间窗口修正活动状态，避免状态滞后导致可报名活动被误拦截。
     */
    private Activity refreshActivityStatusForRegistration(Activity activity, LocalDateTime now) {
        if (activity.getApprovalStatus() != Activity.ApprovalStatus.APPROVED
                || activity.getStatus() == Activity.ActivityStatus.CANCELLED) {
            return activity;
        }

        Activity.ActivityStatus normalizedStatus;
        if (!now.isBefore(activity.getEndTime())) {
            normalizedStatus = Activity.ActivityStatus.COMPLETED;
        } else if (!now.isBefore(activity.getStartTime())) {
            normalizedStatus = Activity.ActivityStatus.ONGOING;
        } else if (!now.isBefore(activity.getRegistrationDeadline())) {
            normalizedStatus = Activity.ActivityStatus.REGISTRATION_CLOSED;
        } else {
            normalizedStatus = Activity.ActivityStatus.PUBLISHED;
        }

        if (activity.getStatus() != normalizedStatus) {
            activity.setStatus(normalizedStatus);
            activity = activityRepository.save(activity);
            log.info("活动状态自动修正: {} -> {}", activity.getTitle(), normalizedStatus);
        }

        return activity;
    }
    
    /**
     * 取消报名
     */
    @Transactional
    public void cancelRegistration(Long activityId, User user) {
        ActivityRegistration registration = registrationRepository
                .findByUserIdAndActivityId(user.getId(), activityId)
                .orElseThrow(() -> new BusinessException("未找到报名记录"));
        
        if (registration.getStatus() != ActivityRegistration.RegistrationStatus.REGISTERED) {
            throw new BusinessException("无法取消该报名");
        }
        
        Activity activity = registration.getActivity();
        if (LocalDateTime.now().isAfter(activity.getStartTime())) {
            throw new BusinessException("活动已开始，无法取消报名");
        }
        
        registration.setStatus(ActivityRegistration.RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());
        registrationRepository.save(registration);
        
        // 更新报名人数
        activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
        activityRepository.save(activity);
        
        log.info("用户取消报名: {} -> {}", user.getUsername(), activity.getTitle());
    }
    
    /**
     * 签到
     */
    @Transactional
    public ActivityRegistrationDTO checkIn(Long activityId, String qrCode, User user) {
        Activity activity = getActivityEntity(activityId);
        
        // 验证签到码
        if (activity.getCheckInType() == Activity.CheckInType.QR_CODE) {
            if (!validateQRCode(activity, qrCode)) {
                throw new BusinessException("签到码无效");
            }
        }
        
        ActivityRegistration registration = registrationRepository
                .findByUserIdAndActivityId(user.getId(), activityId)
                .orElseThrow(() -> new BusinessException("您未报名该活动"));
        
        if (registration.getCheckedIn()) {
            throw new BusinessException("您已签到");
        }
        
        registration.setCheckedIn(true);
        registration.setCheckInTime(LocalDateTime.now());
        registration.setStatus(ActivityRegistration.RegistrationStatus.ATTENDED);
        registration = registrationRepository.save(registration);
        
        // 更新用户参与次数
        clubMemberRepository.findByUserIdAndClubId(user.getId(), activity.getClub().getId())
                .ifPresent(member -> {
                    member.setActivityParticipation(member.getActivityParticipation() + 1);
                    member.setContributionScore(member.getContributionScore() + 1);
                    clubMemberRepository.save(member);
                });
        
        log.info("用户签到: {} -> {}", user.getUsername(), activity.getTitle());
        
        return ActivityRegistrationDTO.fromEntity(registration);
    }
    
    /**
     * 社团负责人手动签到参与者
     */
    @Transactional
    public ActivityRegistrationDTO manualCheckIn(Long activityId, Long userId, User operator) {
        Activity activity = getActivityEntity(activityId);
        
        // 检查权限：只有社团负责人或管理员可以手动签到
        checkActivityManagePermission(activity.getClub(), operator);
        
        ActivityRegistration registration = registrationRepository
                .findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new BusinessException("该用户未报名此活动"));
        
        if (registration.getCheckedIn()) {
            throw new BusinessException("该用户已签到");
        }
        
        registration.setCheckedIn(true);
        registration.setCheckInTime(LocalDateTime.now());
        registration.setStatus(ActivityRegistration.RegistrationStatus.ATTENDED);
        registration = registrationRepository.save(registration);
        
        // 更新用户参与次数
        clubMemberRepository.findByUserIdAndClubId(userId, activity.getClub().getId())
                .ifPresent(member -> {
                    member.setActivityParticipation(member.getActivityParticipation() + 1);
                    member.setContributionScore(member.getContributionScore() + 1);
                    clubMemberRepository.save(member);
                });
        
        log.info("手动签到: {} 为用户ID {} 签到活动 {}", operator.getUsername(), userId, activity.getTitle());
        
        return ActivityRegistrationDTO.fromEntity(registration);
    }
    
    /**
     * 获取活动报名列表
     */
    public Page<ActivityRegistrationDTO> getActivityRegistrations(Long activityId, 
                                                                    ActivityRegistration.RegistrationStatus status,
                                                                    Pageable pageable) {
        if (status == null) {
            return registrationRepository.findByActivityId(activityId, pageable)
                    .map(ActivityRegistrationDTO::fromEntity);
        }
        return registrationRepository.findByActivityIdAndStatus(activityId, status, pageable)
                .map(ActivityRegistrationDTO::fromEntity);
    }
    
    /**
     * 获取用户活动历史
     */
    public Page<ActivityRegistrationDTO> getUserActivityHistory(Long userId, Pageable pageable) {
        return registrationRepository.findUserActivityHistory(userId, pageable)
                .map(ActivityRegistrationDTO::fromEntity);
    }
    
    /**
     * 提交活动反馈
     */
    @Transactional
    public ActivityFeedbackDTO submitFeedback(ActivityFeedbackDTO.CreateRequest request, User user) {
        Activity activity = getActivityEntity(request.getActivityId());
        
        // 检查活动是否已完成
        if (activity.getStatus() != Activity.ActivityStatus.COMPLETED) {
            throw new BusinessException("活动尚未结束，无法评价");
        }
        
        // 检查是否参与了活动
        ActivityRegistration registration = registrationRepository
                .findByUserIdAndActivityId(user.getId(), activity.getId())
                .orElseThrow(() -> new BusinessException("您未参与该活动"));
        
        if (!registration.getCheckedIn()) {
            throw new BusinessException("您未签到，无法评价");
        }
        
        // 检查是否已评价
        if (feedbackRepository.existsByUserIdAndActivityId(user.getId(), activity.getId())) {
            throw new BusinessException("您已评价该活动");
        }
        
        ActivityFeedback feedback = ActivityFeedback.builder()
                .user(user)
                .activity(activity)
                .rating(request.getRating())
                .comment(request.getComment())
                .images(request.getImages())
                .anonymous(request.getAnonymous())
                .build();
        
        feedback = feedbackRepository.save(feedback);
        
        // 更新活动评分
        updateActivityRating(activity);
        
        log.info("用户提交活动反馈: {} -> {}", user.getUsername(), activity.getTitle());
        
        return ActivityFeedbackDTO.fromEntity(feedback);
    }
    
    /**
     * 获取活动反馈列表
     */
    public Page<ActivityFeedbackDTO> getActivityFeedbacks(Long activityId, Pageable pageable) {
        return feedbackRepository.findByActivityIdOrderByCreatedAtDesc(activityId, pageable)
                .map(ActivityFeedbackDTO::fromEntity);
    }

    /**
     * 获取当前用户的反馈
     */
    public Page<ActivityFeedbackDTO> getUserFeedbacks(Long userId, Pageable pageable) {
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ActivityFeedbackDTO::fromEntity);
    }
    
    /**
     * 提交活动总结
     */
    @Transactional
    public ActivityDTO submitSummary(Long activityId, ActivityDTO.SummaryRequest request, User operator) {
        Activity activity = getActivityEntity(activityId);
        checkActivityManagePermission(activity.getClub(), operator);
        
        activity.setSummary(request.getSummary());
        activity.setSummaryImages(request.getSummaryImages());
        activity.setStatus(Activity.ActivityStatus.COMPLETED);
        
        activity = activityRepository.save(activity);
        
        // 更新社团活跃度
        updateClubActivityScore(activity.getClub());
        
        log.info("活动总结提交: {}", activity.getTitle());
        
        return enrichDTO(activity);
    }
    
    /**
     * 生成活动统计报表
     */
    public ActivityStatsDTO generateActivityStats(Long activityId) {
        Activity activity = getActivityEntity(activityId);
        
        long registered = registrationRepository.countRegisteredByActivityId(activityId);
        long checkedIn = registrationRepository.countCheckedInByActivityId(activityId);
        Double avgRating = feedbackRepository.getAverageRatingByActivityId(activityId);
        long feedbackCount = feedbackRepository.countByActivityId(activityId);
        
        return ActivityStatsDTO.builder()
                .activityId(activityId)
                .activityTitle(activity.getTitle())
                .registeredCount((int) registered)
                .checkedInCount((int) checkedIn)
                .attendanceRate(registered > 0 ? (double) checkedIn / registered * 100 : 0)
                .averageRating(avgRating != null ? avgRating : 0)
                .feedbackCount((int) feedbackCount)
                .build();
    }
    
    // 私有辅助方法
    
    private void checkActivityManagePermission(Club club, User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return;
        }
        
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(user.getId(), club.getId())
                .orElseThrow(() -> new BusinessException("您没有权限管理该社团活动"));
        
        if (member.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("您没有权限管理该社团活动");
        }
        
        if (member.getRole() != ClubMember.MemberRole.LEADER && 
            member.getRole() != ClubMember.MemberRole.TEACHER) {
            throw new BusinessException("您没有权限管理该社团活动");
        }
    }
    
    private String generateQRCode(Long activityId) {
        // 生成简短的签到码，而不是完整的图片
        // 前端可以根据这个码生成二维码显示
        try {
            String checkInCode = "CHECKIN_" + activityId + "_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("生成签到码: {}", checkInCode);
            return checkInCode;
        } catch (Exception e) {
            log.error("生成签到码失败", e);
            return "CHECKIN_" + activityId + "_DEFAULT";
        }
    }
    
    private boolean validateQRCode(Activity activity, String qrCode) {
        // 验证签到码格式
        if (qrCode == null || activity.getQrCodeUrl() == null) {
            return false;
        }
        // 简化验证：检查是否匹配存储的签到码
        return qrCode.equals(activity.getQrCodeUrl()) || 
               qrCode.startsWith("CHECKIN_" + activity.getId());
    }
    
    private void updateActivityRating(Activity activity) {
        Double avgRating = feedbackRepository.getAverageRatingByActivityId(activity.getId());
        long count = feedbackRepository.countByActivityId(activity.getId());
        
        activity.setRating(avgRating != null ? avgRating : 0);
        activity.setRatingCount((int) count);
        activityRepository.save(activity);
    }
    
    private void updateClubActivityScore(Club club) {
        long completedCount = activityRepository.countCompletedActivitiesByClub(club.getId());
        // 简单的活跃度计算
        double score = completedCount * 10.0 + club.getMemberCount() * 0.5;
        club.setActivityScore(score);
        clubRepository.save(club);
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ActivityStatsDTO {
        private Long activityId;
        private String activityTitle;
        private Integer registeredCount;
        private Integer checkedInCount;
        private Double attendanceRate;
        private Double averageRating;
        private Integer feedbackCount;
    }
}
