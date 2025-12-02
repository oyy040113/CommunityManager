package com.community.service;

import com.community.dto.AnnouncementDTO;
import com.community.entity.Announcement;
import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.exception.BusinessException;
import com.community.repository.AnnouncementRepository;
import com.community.repository.ClubMemberRepository;
import com.community.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final AnnouncementRepository announcementRepository;
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final NotificationService notificationService;
    
    /**
     * 创建公告
     */
    @Transactional
    public AnnouncementDTO createAnnouncement(AnnouncementDTO.CreateRequest request, User publisher) {
        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new BusinessException("社团不存在"));
        
        // 检查权限
        checkAnnouncementPermission(club, publisher);
        
        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .club(club)
                .publisher(publisher)
                .type(request.getType())
                .pinned(request.getPinned())
                .viewCount(0)
                .published(true)
                .expireAt(request.getExpireAt())
                .build();
        
        announcement = announcementRepository.save(announcement);
        
        // 通知社团成员
        notificationService.sendAnnouncementNotification(announcement);
        
        log.info("公告发布: {} by {}", announcement.getTitle(), publisher.getUsername());
        
        return AnnouncementDTO.fromEntity(announcement);
    }
    
    /**
     * 更新公告
     */
    @Transactional
    public AnnouncementDTO updateAnnouncement(Long announcementId, 
                                               AnnouncementDTO.UpdateRequest request, 
                                               User operator) {
        Announcement announcement = getAnnouncementEntity(announcementId);
        
        // 检查权限
        checkAnnouncementPermission(announcement.getClub(), operator);
        
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getType() != null) {
            announcement.setType(request.getType());
        }
        if (request.getPinned() != null) {
            announcement.setPinned(request.getPinned());
        }
        if (request.getPublished() != null) {
            announcement.setPublished(request.getPublished());
        }
        if (request.getExpireAt() != null) {
            announcement.setExpireAt(request.getExpireAt());
        }
        
        announcement = announcementRepository.save(announcement);
        log.info("公告更新: {}", announcement.getTitle());
        
        return AnnouncementDTO.fromEntity(announcement);
    }
    
    /**
     * 获取公告详情
     */
    @Transactional
    public AnnouncementDTO getAnnouncementById(Long announcementId) {
        Announcement announcement = getAnnouncementEntity(announcementId);
        
        // 增加浏览次数
        announcement.setViewCount(announcement.getViewCount() + 1);
        announcementRepository.save(announcement);
        
        return AnnouncementDTO.fromEntity(announcement);
    }
    
    /**
     * 获取公告实体
     */
    public Announcement getAnnouncementEntity(Long announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException("公告不存在"));
    }
    
    /**
     * 获取社团公告列表
     */
    public Page<AnnouncementDTO> getClubAnnouncements(Long clubId, Pageable pageable) {
        return announcementRepository.findActiveAnnouncementsByClubId(
                clubId, LocalDateTime.now(), pageable)
                .map(AnnouncementDTO::fromEntity);
    }
    
    /**
     * 获取用户相关公告（所加入社团的公告）
     */
    public List<AnnouncementDTO> getUserRelatedAnnouncements(Long userId, int limit) {
        List<Long> clubIds = clubMemberRepository.findApprovedMembershipsByUserId(userId)
                .stream()
                .map(member -> member.getClub().getId())
                .collect(Collectors.toList());
        
        if (clubIds.isEmpty()) {
            return List.of();
        }
        
        return announcementRepository.findRecentAnnouncementsForClubs(
                clubIds, LocalDateTime.now(), PageRequest.of(0, limit))
                .stream()
                .map(AnnouncementDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long announcementId, User operator) {
        Announcement announcement = getAnnouncementEntity(announcementId);
        
        // 检查权限
        checkAnnouncementPermission(announcement.getClub(), operator);
        
        announcementRepository.delete(announcement);
        log.info("公告删除: {}", announcement.getTitle());
    }
    
    /**
     * 检查公告管理权限
     */
    private void checkAnnouncementPermission(Club club, User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return;
        }
        
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(user.getId(), club.getId())
                .orElseThrow(() -> new BusinessException("您没有权限管理该社团公告"));
        
        if (member.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("您没有权限管理该社团公告");
        }
        
        if (member.getRole() != ClubMember.MemberRole.LEADER && 
            member.getRole() != ClubMember.MemberRole.VICE_LEADER &&
            member.getRole() != ClubMember.MemberRole.ADMIN &&
            member.getRole() != ClubMember.MemberRole.PUBLICITY) {
            throw new BusinessException("您没有权限管理该社团公告");
        }
    }
}
