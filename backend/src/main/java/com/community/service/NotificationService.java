package com.community.service;

import com.community.entity.*;
import com.community.repository.ClubMemberRepository;
import com.community.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ClubMemberRepository clubMemberRepository;
    
    /**
     * 发送社团加入申请通知
     */
    @Transactional
    public void sendClubJoinNotification(User receiver, User applicant, Club club) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title("新的社团加入申请")
                .content(String.format("用户 %s 申请加入社团 %s", applicant.getRealName(), club.getName()))
                .type(Notification.NotificationType.CLUB_JOIN)
                .relatedId(club.getId())
                .relatedType("club")
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * 发送申请通过通知
     */
    @Transactional
    public void sendApplicationApprovedNotification(User receiver, Club club) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title("社团申请已通过")
                .content(String.format("恭喜您！您加入社团 %s 的申请已通过", club.getName()))
                .type(Notification.NotificationType.CLUB_APPROVED)
                .relatedId(club.getId())
                .relatedType("club")
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * 发送申请拒绝通知
     */
    @Transactional
    public void sendApplicationRejectedNotification(User receiver, Club club, String reason) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title("社团申请未通过")
                .content(String.format("很抱歉，您加入社团 %s 的申请未通过。原因：%s", 
                        club.getName(), reason != null ? reason : "无"))
                .type(Notification.NotificationType.CLUB_REJECTED)
                .relatedId(club.getId())
                .relatedType("club")
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * 发送活动发布通知
     */
    @Transactional
    public void sendActivityPublishedNotification(Activity activity) {
        Club club = activity.getClub();
        List<ClubMember> members = clubMemberRepository.findByClubId(club.getId());
        
        for (ClubMember member : members) {
            if (member.getStatus() == ClubMember.MemberStatus.APPROVED) {
                Notification notification = Notification.builder()
                        .user(member.getUser())
                        .title("新活动发布")
                        .content(String.format("社团 %s 发布了新活动：%s", 
                                club.getName(), activity.getTitle()))
                        .type(Notification.NotificationType.ACTIVITY_UPDATE)
                        .relatedId(activity.getId())
                        .relatedType("activity")
                        .build();
                
                notificationRepository.save(notification);
            }
        }
    }
    
    /**
     * 发送活动提醒通知
     */
    @Transactional
    public void sendActivityReminderNotification(User receiver, Activity activity) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title("活动即将开始")
                .content(String.format("活动 %s 即将于 %s 开始，请准时参加", 
                        activity.getTitle(), activity.getStartTime().toString()))
                .type(Notification.NotificationType.ACTIVITY_REMINDER)
                .relatedId(activity.getId())
                .relatedType("activity")
                .build();
        
        notificationRepository.save(notification);
    }
    
    /**
     * 发送公告通知
     */
    @Transactional
    public void sendAnnouncementNotification(Announcement announcement) {
        Club club = announcement.getClub();
        List<ClubMember> members = clubMemberRepository.findByClubId(club.getId());
        
        for (ClubMember member : members) {
            if (member.getStatus() == ClubMember.MemberStatus.APPROVED) {
                Notification notification = Notification.builder()
                        .user(member.getUser())
                        .title("新公告")
                        .content(String.format("社团 %s 发布了新公告：%s", 
                                club.getName(), announcement.getTitle()))
                        .type(Notification.NotificationType.ANNOUNCEMENT)
                        .relatedId(announcement.getId())
                        .relatedType("announcement")
                        .build();
                
                notificationRepository.save(notification);
            }
        }
    }
    
    /**
     * 获取用户通知列表
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * 获取未读通知
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 获取未读通知数量
     */
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
