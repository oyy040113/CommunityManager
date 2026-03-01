package com.community.service;

import com.community.dto.ClubDTO;
import com.community.dto.ClubMemberDTO;
import com.community.entity.Club;
import com.community.entity.ClubMember;
import com.community.entity.User;
import com.community.exception.BusinessException;
import com.community.repository.ClubMemberRepository;
import com.community.repository.ClubRepository;
import com.community.repository.UserRepository;
import com.community.repository.ActivityFeedbackRepository;
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
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityFeedbackRepository activityFeedbackRepository;
    
    /**
     * 将Club实体转换为DTO并填充评价统计
     */
    private ClubDTO enrichDTO(Club club) {
        ClubDTO dto = ClubDTO.fromEntity(club);
        dto.setFeedbackCount((int) activityFeedbackRepository.countByClubId(club.getId()));
        Double avgRating = activityFeedbackRepository.getAverageRatingByClubId(club.getId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        return dto;
    }
    
    /**
     * 创建社团（用户创建，需要审批）
     */
    @Transactional
    public ClubDTO createClub(ClubDTO.CreateRequest request, User creator) {
        // 检查社团名称是否已存在
        if (clubRepository.existsByName(request.getName())) {
            throw new BusinessException("社团名称已存在");
        }
        
        // 用户创建社团，状态为待审批
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
                .creator(creator)
                .leader(creator)
                .memberCount(0)
                .activityCount(0)
                .activityScore(0.0)
                .status(Club.ClubStatus.PENDING) // 待审批
                .build();
        
        club = clubRepository.save(club);
        
        log.info("社团创建申请提交: {} by {}", club.getName(), creator.getUsername());
        
        return enrichDTO(club);
    }
    
    /**
     * 管理员直接创建社团（无需审批）
     */
    @Transactional
    public ClubDTO createClubByAdmin(ClubDTO.CreateRequest request, User admin, Long leaderId, UserService userService) {
        // 检查社团名称是否已存在
        if (clubRepository.existsByName(request.getName())) {
            throw new BusinessException("社团名称已存在");
        }
        
        User leader = userService.getUserById(leaderId);
        
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
                .creator(admin)
                .leader(leader)
                .memberCount(1)
                .activityCount(0)
                .activityScore(0.0)
                .status(Club.ClubStatus.ACTIVE) // 管理员创建直接激活
                .approvedBy(admin)
                .approvedAt(LocalDateTime.now())
                .build();
        
        club = clubRepository.save(club);
        
        // 添加负责人为社团成员
        ClubMember leaderMember = ClubMember.builder()
                .user(leader)
                .club(club)
                .role(ClubMember.MemberRole.LEADER)
                .position("社长")
                .status(ClubMember.MemberStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .joinedAt(LocalDateTime.now())
                .activityParticipation(0)
                .contributionScore(0.0)
                .build();
        clubMemberRepository.save(leaderMember);
        
        log.info("管理员创建社团: {} leader: {}", club.getName(), leader.getUsername());
        
        return enrichDTO(club);
    }
    
    /**
     * 审批社团创建申请（管理员）
     */
    @Transactional
    public ClubDTO approveClub(Long clubId, ClubDTO.ApprovalRequest request, User admin) {
        Club club = getClubEntity(clubId);
        
        if (club.getStatus() != Club.ClubStatus.PENDING) {
            throw new BusinessException("该社团不在待审批状态");
        }
        
        if (request.getApproved()) {
            club.setStatus(Club.ClubStatus.ACTIVE);
            club.setApprovedBy(admin);
            club.setApprovedAt(LocalDateTime.now());
            club.setMemberCount(1);
            
            // 添加创建者为社团负责人成员
            ClubMember leaderMember = ClubMember.builder()
                    .user(club.getCreator())
                    .club(club)
                    .role(ClubMember.MemberRole.LEADER)
                    .position("社长")
                    .status(ClubMember.MemberStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .joinedAt(LocalDateTime.now())
                    .activityParticipation(0)
                    .contributionScore(0.0)
                    .build();
            clubMemberRepository.save(leaderMember);
            
            // 如果创建者是普通用户，自动升级为社团负责人角色
            User creator = club.getCreator();
            if (creator.getRole() == User.UserRole.USER) {
                creator.setRole(User.UserRole.CLUB_LEADER);
                userRepository.save(creator);
                log.info("用户 {} 升级为社团负责人", creator.getUsername());
            }
            
            // 通知创建者
            notificationService.sendClubApprovedNotification(club.getCreator(), club);
            
            log.info("社团审批通过: {}", club.getName());
        } else {
            club.setStatus(Club.ClubStatus.REJECTED);
            club.setRejectReason(request.getRejectReason());
            
            // 通知创建者
            notificationService.sendClubRejectedNotification(club.getCreator(), club, request.getRejectReason());
            
            log.info("社团审批拒绝: {}", club.getName());
        }
        
        club = clubRepository.save(club);
        return enrichDTO(club);
    }
    
    /**
     * 获取待审批社团列表（管理员）
     */
    public Page<ClubDTO> getPendingClubs(Pageable pageable) {
        return clubRepository.findPendingClubs(pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 管理员搜索社团（支持所有状态）
     */
    public Page<ClubDTO> searchClubsAdmin(String keyword, Club.ClubType type, 
                                           Club.ClubStatus status, Pageable pageable) {
        return clubRepository.searchClubsAdmin(keyword, type, status, pageable)
                .map(this::enrichDTO);
    }
    
    /**
     * 删除社团（管理员或社团创建者）
     */
    @Transactional
    public void deleteClub(Long clubId, User operator) {
        Club club = getClubEntity(clubId);
        
        // 检查权限：只有管理员或社团创建者可以删除
        if (operator.getRole() != User.UserRole.ADMIN) {
            if (club.getCreator() == null || !club.getCreator().getId().equals(operator.getId())) {
                throw new BusinessException("您没有权限删除该社团");
            }
        }
        
        // 设置状态为解散
        club.setStatus(Club.ClubStatus.DISSOLVED);
        clubRepository.save(club);
        
        log.info("社团已删除: {} by {}", club.getName(), operator.getUsername());
    }
    
    /**
     * 转让社团（社团创建者可以将社团转让给其他成员）
     */
    @Transactional
    public ClubDTO transferClub(Long clubId, ClubDTO.TransferRequest request, User operator) {
        Club club = getClubEntity(clubId);
        
        // 只有社团创建者或管理员可以转让
        if (operator.getRole() != User.UserRole.ADMIN) {
            if (club.getCreator() == null || !club.getCreator().getId().equals(operator.getId())) {
                throw new BusinessException("只有社团创建者可以转让社团");
            }
        }
        
        // 检查新负责人是否是社团成员
        ClubMember newLeaderMember = clubMemberRepository.findByUserIdAndClubId(request.getNewLeaderId(), clubId)
                .orElseThrow(() -> new BusinessException("新负责人不是该社团成员"));
        
        if (newLeaderMember.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("新负责人的成员资格未通过审核");
        }
        
        // 更新旧负责人角色
        ClubMember oldLeaderMember = clubMemberRepository.findByUserIdAndClubId(club.getLeader().getId(), clubId)
                .orElse(null);
        if (oldLeaderMember != null) {
            oldLeaderMember.setRole(ClubMember.MemberRole.MEMBER);
            oldLeaderMember.setPosition(null);
            clubMemberRepository.save(oldLeaderMember);
        }
        
        // 更新新负责人角色
        newLeaderMember.setRole(ClubMember.MemberRole.LEADER);
        newLeaderMember.setPosition("社长");
        clubMemberRepository.save(newLeaderMember);
        
        // 更新社团负责人
        club.setLeader(newLeaderMember.getUser());
        club = clubRepository.save(club);
        
        log.info("社团转让: {} -> {}", club.getName(), newLeaderMember.getUser().getUsername());
        
        return enrichDTO(club);
    }
    
    /**
     * 设置社团管理员（社团创建者可以设置其他人为社团管理员）
     */
    @Transactional
    public ClubMemberDTO setClubAdmin(Long clubId, ClubDTO.SetAdminRequest request, User operator) {
        Club club = getClubEntity(clubId);
        
        // 检查权限：只有社团创建者或系统管理员可以设置
        if (operator.getRole() != User.UserRole.ADMIN) {
            if (club.getCreator() == null || !club.getCreator().getId().equals(operator.getId())) {
                // 也允许社团负责人操作
                if (club.getLeader() == null || !club.getLeader().getId().equals(operator.getId())) {
                    throw new BusinessException("只有社团创建者或负责人可以设置管理员");
                }
            }
        }
        
        // 检查目标用户是否是社团成员
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(request.getUserId(), clubId)
                .orElseThrow(() -> new BusinessException("该用户不是社团成员"));
        
        if (member.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("该成员资格未通过审核");
        }
        
        // 不能修改社长的角色
        if (member.getRole() == ClubMember.MemberRole.LEADER) {
            throw new BusinessException("不能修改社长的角色");
        }
        
        if (request.getIsAdmin()) {
            member.setRole(ClubMember.MemberRole.TEACHER);
            member.setPosition(request.getPosition() != null ? request.getPosition() : "指导老师");
        } else {
            member.setRole(ClubMember.MemberRole.MEMBER);
            member.setPosition(null);
        }
        
        member = clubMemberRepository.save(member);
        log.info("社团管理员设置: {} in {} -> {}", 
                member.getUser().getUsername(), club.getName(), request.getIsAdmin() ? "管理员" : "普通成员");
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 移除社团成员
     */
    @Transactional
    public void removeMember(Long clubId, Long userId, User operator) {
        Club club = getClubEntity(clubId);
        
        // 检查权限
        checkClubManagePermission(club, operator);
        
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(userId, clubId)
                .orElseThrow(() -> new BusinessException("该用户不是社团成员"));
        
        // 不能移除社长
        if (member.getRole() == ClubMember.MemberRole.LEADER) {
            throw new BusinessException("不能移除社团负责人");
        }
        
        member.setStatus(ClubMember.MemberStatus.REMOVED);
        clubMemberRepository.save(member);
        
        // 更新社团成员数
        club.setMemberCount(Math.max(0, club.getMemberCount() - 1));
        clubRepository.save(club);
        
        log.info("成员被移除: {} from {}", member.getUser().getUsername(), club.getName());
    }
    
    /**
     * 统计待审批社团数
     */
    public long countPendingClubs() {
        return clubRepository.countPendingClubs();
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
        
        return enrichDTO(club);
    }
    
    /**
     * 获取社团详情
     */
    public ClubDTO getClubById(Long clubId) {
        return enrichDTO(getClubEntity(clubId));
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
                .map(this::enrichDTO);
    }
    
    /**
     * 获取热门社团
     */
    public List<ClubDTO> getTopActiveClubs(int limit) {
        return clubRepository.findTopActiveClubs(PageRequest.of(0, limit))
                .stream()
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户管理的社团
     */
    public List<ClubDTO> getClubsByLeader(Long leaderId) {
        return clubRepository.findByLeaderId(leaderId)
                .stream()
                .map(this::enrichDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户创建的社团（包括待审批、已拒绝等所有状态）
     */
    public List<ClubDTO> getClubsByCreator(Long creatorId) {
        return clubRepository.findByCreatorId(creatorId)
                .stream()
                .map(this::enrichDTO)
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
                .joinedAt(LocalDateTime.now())
                .activityParticipation(0)
                .contributionScore(0.0)
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
     * 获取用户管理的社团的所有待审核申请
     */
    public List<ClubMemberDTO> getPendingApprovalsForLeader(Long userId) {
        // 获取用户管理的所有社团
        List<Club> managedClubs = clubRepository.findByLeaderId(userId);
        
        // 获取这些社团的所有待审核申请
        return managedClubs.stream()
                .flatMap(club -> clubMemberRepository.findPendingApplicationsByClubId(club.getId(), Pageable.unpaged())
                        .getContent().stream())
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
        
        // 社团创建者始终有管理权限
        if (club.getLeader() != null && club.getLeader().getId().equals(user.getId())) {
            return;
        }
        
        ClubMember member = clubMemberRepository.findByUserIdAndClubId(user.getId(), club.getId())
                .orElseThrow(() -> new BusinessException("您没有权限管理该社团"));
        
        if (member.getStatus() != ClubMember.MemberStatus.APPROVED) {
            throw new BusinessException("您没有权限管理该社团");
        }
        
        if (member.getRole() != ClubMember.MemberRole.LEADER && 
            member.getRole() != ClubMember.MemberRole.TEACHER) {
            throw new BusinessException("您没有权限管理该社团");
        }
    }
    
    /**
     * 统计活跃社团数
     */
    public long countActiveClubs() {
        return clubRepository.countActiveClubs();
    }
    
    /**
     * 邀请指导老师加入社团
     */
    @Transactional
    public ClubMemberDTO inviteTeacher(Long clubId, ClubMemberDTO.InviteTeacherRequest request, User operator) {
        Club club = getClubEntity(clubId);
        
        // 检查权限：只有社团负责人可以邀请教师
        checkClubManagePermission(club, operator);
        
        User teacher = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("教师不存在"));
        
        // 检查被邀请者是否为教师角色
        if (teacher.getRole() != User.UserRole.TEACHER) {
            throw new BusinessException("只能邀请教师角色的用户");
        }
        
        // 检查是否已经是成员
        if (clubMemberRepository.existsByUserIdAndClubId(teacher.getId(), club.getId())) {
            ClubMember existing = clubMemberRepository.findByUserIdAndClubId(teacher.getId(), club.getId())
                    .orElseThrow();
            if (existing.getStatus() == ClubMember.MemberStatus.APPROVED) {
                throw new BusinessException("该教师已经是该社团成员");
            } else if (existing.getStatus() == ClubMember.MemberStatus.PENDING) {
                throw new BusinessException("该教师邀请处理中");
            }
        }
        
        ClubMember member = ClubMember.builder()
                .user(teacher)
                .club(club)
                .role(ClubMember.MemberRole.TEACHER)
                .position(request.getPosition() != null ? request.getPosition() : "指导老师")
                .status(ClubMember.MemberStatus.PENDING) // 待老师接受
                .applicationReason(request.getInvitationReason()) // 使用 applicationReason 存储邀请理由
                .joinedAt(LocalDateTime.now())
                .activityParticipation(0)
                .contributionScore(0.0)
                .build();
        
        member = clubMemberRepository.save(member);
        
        // 通知教师有邀请
        notificationService.sendClubJoinNotification(teacher, operator, club);
        
        log.info("教师邀请已发送: {} 到社团 {}", teacher.getUsername(), club.getName());
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 教师接受邀请
     */
    @Transactional
    public ClubMemberDTO acceptTeacherInvitation(Long memberId, User currentUser) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("邀请记录不存在"));
        
        // 检查权限：只有被邀请的教师可以接受邀请
        if (!member.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("您没有权限接受此邀请");
        }
        
        if (member.getStatus() != ClubMember.MemberStatus.PENDING) {
            throw new BusinessException("该邀请已处理");
        }
        
        if (member.getRole() != ClubMember.MemberRole.TEACHER) {
            throw new BusinessException("只有教师邀请可以接受");
        }
        
        member.setStatus(ClubMember.MemberStatus.APPROVED);
        member.setApprovedAt(LocalDateTime.now());
        
        // 更新社团成员数
        Club club = member.getClub();
        club.setMemberCount(club.getMemberCount() + 1);
        clubRepository.save(club);
        
        member = clubMemberRepository.save(member);
        
        log.info("教师接受邀请: {} 加入社团 {}", currentUser.getUsername(), club.getName());
        
        return ClubMemberDTO.fromEntity(member);
    }
    
    /**
     * 教师拒绝邀请
     */
    @Transactional
    public void rejectTeacherInvitation(Long memberId, User currentUser) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("邀请记录不存在"));
        
        // 检查权限：只有被邀请的教师可以拒绝邀请
        if (!member.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException("您没有权限处理此邀请");
        }
        
        if (member.getStatus() != ClubMember.MemberStatus.PENDING) {
            throw new BusinessException("该邀请已处理");
        }
        
        if (member.getRole() != ClubMember.MemberRole.TEACHER) {
            throw new BusinessException("只有教师邀请可以拒绝");
        }
        
        member.setStatus(ClubMember.MemberStatus.REJECTED);
        clubMemberRepository.save(member);
        
        log.info("教师拒绝邀请: {} 拒绝加入社团 {}", currentUser.getUsername(), member.getClub().getName());
    }
    
    /**
     * 获取指导老师邀请列表
     */
    public List<ClubMemberDTO> getTeacherInvitations(Long userId) {
        List<ClubMember> invitations = clubMemberRepository.findByUserIdAndRole(userId, ClubMember.MemberRole.TEACHER);
        return invitations.stream()
                .filter(m -> m.getStatus() == ClubMember.MemberStatus.PENDING)
                .map(ClubMemberDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
