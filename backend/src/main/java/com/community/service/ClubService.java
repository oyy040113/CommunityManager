package com.community.service;

import com.community.dto.ClubDTO;
import com.community.dto.ClubMemberDTO;
import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.exception.BusinessException;
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
 * 社团服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {
    
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final NotificationService notificationService;
    
    /**
     * 创建社团
     */
    @Transactional
    public ClubDTO createClub(ClubDTO.CreateRequest request, User leader) {
        // 检查社团名称是否已存在
        if (clubRepository.existsByName(request.getName())) {
            throw new BusinessException("社团名称已存在");
        }
        
        Club club = Club.builder()
                .name(request.getName())
                .type(request.getType())
                .purpose(request.getPurpose())
                .description(request.getDescription())
                .logo(request.getLogo())
                .coverImage(request.getCoverImage())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .location(request.getLocation())
                .leader(leader)
                .memberCount(1)
                .activityCount(0)
                .activityScore(0.0)
                .status(Club.ClubStatus.ACTIVE)
                .build();
        
        club = clubRepository.save(club);
        
        // 添加创建者为社团负责人
        ClubMember leaderMember = ClubMember.builder()
                .user(leader)
                .club(club)
                .role(ClubMember.MemberRole.LEADER)
                .position("社长")
                .status(ClubMember.MemberStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();
        clubMemberRepository.save(leaderMember);
        
        log.info("社团创建成功: {} by {}", club.getName(), leader.getUsername());
        
        return ClubDTO.fromEntity(club);
    }
    
    /**
     * 更新社团信息
     */
    @Transactional
    public ClubDTO updateClub(Long clubId, ClubDTO.UpdateRequest request, User operator) {
        Club club = getClubEntity(clubId);
        
        // 检查权限
        checkClubManagePermission(club, operator);
        
        if (request.getName() != null && !request.getName().equals(club.getName())) {
            if (clubRepository.existsByName(request.getName())) {
                throw new BusinessException("社团名称已存在");
            }
            club.setName(request.getName());
        }
        if (request.getType() != null) {
            club.setType(request.getType());
        }
        if (request.getPurpose() != null) {
            club.setPurpose(request.getPurpose());
        }
        if (request.getDescription() != null) {
            club.setDescription(request.getDescription());
        }
        if (request.getLogo() != null) {
            club.setLogo(request.getLogo());
        }
        if (request.getCoverImage() != null) {
            club.setCoverImage(request.getCoverImage());
        }
        if (request.getContactEmail() != null) {
            club.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            club.setContactPhone(request.getContactPhone());
        }
        if (request.getLocation() != null) {
            club.setLocation(request.getLocation());
        }
        if (request.getDocumentUrl() != null) {
            club.setDocumentUrl(request.getDocumentUrl());
        }
        
        club = clubRepository.save(club);
        log.info("社团信息更新: {}", club.getName());
        
        return ClubDTO.fromEntity(club);
    }
    
    /**
     * 获取社团详情
     */
    public ClubDTO getClubById(Long clubId) {
        return ClubDTO.fromEntity(getClubEntity(clubId));
    }
    
    /**
     * 获取社团实体
     */
    public Club getClubEntity(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BusinessException("社团不存在"));
    }
    
    /**
     * 搜索社团
     */
    public Page<ClubDTO> searchClubs(String keyword, Club.ClubType type, Pageable pageable) {
        return clubRepository.searchClubs(keyword, type, Club.ClubStatus.ACTIVE, pageable)
                .map(ClubDTO::fromEntity);
    }
    
    /**
     * 获取热门社团
     */
    public List<ClubDTO> getTopActiveClubs(int limit) {
        return clubRepository.findTopActiveClubs(PageRequest.of(0, limit))
                .stream()
                .map(ClubDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户管理的社团
     */
    public List<ClubDTO> getClubsByLeader(Long leaderId) {
        return clubRepository.findByLeaderId(leaderId)
                .stream()
                .map(ClubDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 申请加入社团
     */
    @Transactional
    public ClubMemberDTO applyToJoinClub(ClubMemberDTO.JoinRequest request, User user) {
        Club club = getClubEntity(request.getClubId());
        
        // 检查是否已经是成员或已申请
        if (clubMemberRepository.existsByUserIdAndClubId(user.getId(), club.getId())) {
            ClubMember existing = clubMemberRepository.findByUserIdAndClubId(user.getId(), club.getId())
                    .orElseThrow();
            if (existing.getStatus() == ClubMember.MemberStatus.APPROVED) {
                throw new BusinessException("您已经是该社团成员");
            } else if (existing.getStatus() == ClubMember.MemberStatus.PENDING) {
                throw new BusinessException("您的申请正在审核中");
            }
        }
        
        ClubMember member = ClubMember.builder()
                .user(user)
                .club(club)
                .role(ClubMember.MemberRole.MEMBER)
                .status(ClubMember.MemberStatus.PENDING)
                .applicationReason(request.getApplicationReason())
                .build();
        
        member = clubMemberRepository.save(member);
        
        // 通知社团负责人
        notificationService.sendClubJoinNotification(club.getLeader(), user, club);
        
        log.info("用户申请加入社团: {} -> {}", user.getUsername(), club.getName());
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 审核加入申请
     */
    @Transactional
    public ClubMemberDTO reviewApplication(Long memberId, ClubMemberDTO.ReviewRequest request, User operator) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("申请记录不存在"));
        
        // 检查权限
        checkClubManagePermission(member.getClub(), operator);
        
        if (member.getStatus() != ClubMember.MemberStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }
        
        if (request.getApproved()) {
            member.setStatus(ClubMember.MemberStatus.APPROVED);
            member.setApprovedAt(LocalDateTime.now());
            
            // 更新社团成员数
            Club club = member.getClub();
            club.setMemberCount(club.getMemberCount() + 1);
            clubRepository.save(club);
            
            // 通知申请人
            notificationService.sendApplicationApprovedNotification(member.getUser(), member.getClub());
        } else {
            member.setStatus(ClubMember.MemberStatus.REJECTED);
            member.setRejectReason(request.getRejectReason());
            
            // 通知申请人
            notificationService.sendApplicationRejectedNotification(
                    member.getUser(), member.getClub(), request.getRejectReason());
        }
        
        member = clubMemberRepository.save(member);
        log.info("申请审核: {} -> {}", member.getUser().getUsername(), 
                request.getApproved() ? "通过" : "拒绝");
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 更新成员角色
     */
    @Transactional
    public ClubMemberDTO updateMemberRole(Long memberId, ClubMemberDTO.UpdateRoleRequest request, User operator) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("成员记录不存在"));
        
        // 检查权限
        checkClubManagePermission(member.getClub(), operator);
        
        member.setRole(request.getRole());
        if (request.getPosition() != null) {
            member.setPosition(request.getPosition());
        }
        
        member = clubMemberRepository.save(member);
        log.info("成员角色更新: {} in {} -> {}", 
                member.getUser().getUsername(), member.getClub().getName(), request.getRole());
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 获取社团成员列表
     */
    public Page<ClubMemberDTO> getClubMembers(Long clubId, ClubMember.MemberStatus status, Pageable pageable) {
        return clubMemberRepository.findByClubIdAndStatus(clubId, status, pageable)
                .map(ClubMemberDTO::fromEntity);
    }
    
    /**
     * 获取待审核申请
     */
    public Page<ClubMemberDTO> getPendingApplications(Long clubId, Pageable pageable) {
        return clubMemberRepository.findPendingApplicationsByClubId(clubId, pageable)
                .map(ClubMemberDTO::fromEntity);
    }
    
    /**
     * 获取用户加入的社团
     */
    public List<ClubMemberDTO> getUserMemberships(Long userId) {
        return clubMemberRepository.findApprovedMembershipsByUserId(userId)
                .stream()
                .map(ClubMemberDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的待审核申请
     */
    public List<ClubMemberDTO> getUserPendingApplications(Long userId) {
        return clubMemberRepository.findPendingApplicationsByUserId(userId)
                .stream()
                .map(ClubMemberDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 退出社团
     */
    @Transactional
    public void leaveClub(Long clubId, User user) {
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId)
                .orElseThrow(() -> new BusinessException("您不是该社团成员"));
        
        if (member.getRole() == ClubMember.MemberRole.LEADER) {
            throw new BusinessException("社团负责人不能直接退出，请先转让社团");
        }
        
        member.setStatus(ClubMember.MemberStatus.WITHDRAWN);
        clubMemberRepository.save(member);
        
        // 更新社团成员数
        Club club = member.getClub();
        club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
        clubRepository.save(club);
        
        log.info("用户退出社团: {} from {}", user.getUsername(), club.getName());
    }
    
    /**
     * 检查社团管理权限
     */
    private void checkClubManagePermission(Club club, User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            return; // 管理员有所有权限
        }
        
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(user.getId(), club.getId())
                .orElseThrow(() -> new BusinessException("您没有权限管理该社团"));
        
        if (member.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("您没有权限管理该社团");
        }
        
        if (member.getRole() != ClubMember.MemberRole.LEADER && 
            member.getRole() != ClubMember.MemberRole.VICE_LEADER &&
            member.getRole() != ClubMember.MemberRole.ADMIN) {
            throw new BusinessException("您没有权限管理该社团");
        }
    }
    
    /**
     * 统计活跃社团数
     */
    public long countActiveClubs() {
        return clubRepository.countActiveClubs();
    }
}
