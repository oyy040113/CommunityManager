package com.community.repository;

import com.community.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 社团数据访问层
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    
    Optional<Club> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Club> findByLeaderId(Long leaderId);
    
    List<Club> findByCreatorId(Long creatorId);
    
    Page<Club> findByType(Club.ClubType type, Pageable pageable);
    
    Page<Club> findByStatus(Club.ClubStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Club c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "c.status = :status " +
           "ORDER BY c.activityScore DESC")
    Page<Club> searchClubs(@Param("keyword") String keyword, 
                           @Param("type") Club.ClubType type,
                           @Param("status") Club.ClubStatus status,
                           Pageable pageable);
    
    @Query("SELECT c FROM Club c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    Page<Club> searchClubsAdmin(@Param("keyword") String keyword, 
                                @Param("type") Club.ClubType type,
                                @Param("status") Club.ClubStatus status,
                                Pageable pageable);
    
    @Query("SELECT c FROM Club c WHERE c.status = 'PENDING' ORDER BY c.createdAt DESC")
    Page<Club> findPendingClubs(Pageable pageable);
    
    @Query("SELECT c FROM Club c WHERE c.status = 'ACTIVE' ORDER BY c.activityScore DESC")
    List<Club> findTopActiveClubs(Pageable pageable);
    
    @Query("SELECT c FROM Club c WHERE c.status = 'ACTIVE' ORDER BY c.memberCount DESC")
    List<Club> findTopClubsByMemberCount(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Club c WHERE c.status = 'ACTIVE'")
    long countActiveClubs();
    
    @Query("SELECT COUNT(c) FROM Club c WHERE c.status = 'PENDING'")
    long countPendingClubs();
    
    @Query("SELECT c.type, COUNT(c) FROM Club c WHERE c.status = 'ACTIVE' GROUP BY c.type")
    List<Object[]> countClubsByType();
}
