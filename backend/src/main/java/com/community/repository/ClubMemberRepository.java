package com.community.repository;

import com.community.entity.ClubMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 社团成员数据访问层
 */
@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
    
    Optional<ClubMember> findByUserIdAndClubId(Long userId, Long clubId);
    
    boolean existsByUserIdAndClubId(Long userId, Long clubId);
    
    List<ClubMember> findByUserId(Long userId);
    
    List<ClubMember> findByClubId(Long clubId);
    
    Page<ClubMember> findByClubIdAndStatus(Long clubId, ClubMember.MemberStatus status, Pageable pageable);
    
    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = 'APPROVED' " +
           "ORDER BY cm.contributionScore DESC")
    List<ClubMember> findTopContributors(@Param("clubId") Long clubId, Pageable pageable);
    
    @Query("SELECT cm FROM ClubMember cm WHERE cm.user.id = :userId AND cm.status = 'APPROVED'")
    List<ClubMember> findApprovedMembershipsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT cm FROM ClubMember cm WHERE cm.user.id = :userId AND cm.status = 'PENDING'")
    List<ClubMember> findPendingApplicationsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = 'PENDING'")
    Page<ClubMember> findPendingApplicationsByClubId(@Param("clubId") Long clubId, Pageable pageable);
    
    @Query("SELECT COUNT(cm) FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = 'APPROVED'")
    long countApprovedMembers(@Param("clubId") Long clubId);
    
    @Query("SELECT COUNT(cm) FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = 'PENDING'")
    long countPendingApplications(@Param("clubId") Long clubId);
    
    @Query("SELECT cm.role, COUNT(cm) FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = 'APPROVED' GROUP BY cm.role")
    List<Object[]> countMembersByRole(@Param("clubId") Long clubId);
}
