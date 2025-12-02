package com.community.repository;

import com.community.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告数据访问层
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    Page<Announcement> findByClubIdOrderByPinnedDescCreatedAtDesc(Long clubId, Pageable pageable);
    
    @Query("SELECT a FROM Announcement a WHERE a.club.id = :clubId AND a.published = true " +
           "AND (a.expireAt IS NULL OR a.expireAt > :now) " +
           "ORDER BY a.pinned DESC, a.createdAt DESC")
    Page<Announcement> findActiveAnnouncementsByClubId(@Param("clubId") Long clubId, 
                                                        @Param("now") LocalDateTime now,
                                                        Pageable pageable);
    
    @Query("SELECT a FROM Announcement a WHERE a.club.id IN :clubIds AND a.published = true " +
           "AND (a.expireAt IS NULL OR a.expireAt > :now) " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findRecentAnnouncementsForClubs(@Param("clubIds") List<Long> clubIds,
                                                        @Param("now") LocalDateTime now,
                                                        Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Announcement a WHERE a.club.id = :clubId AND a.published = true")
    long countPublishedByClubId(@Param("clubId") Long clubId);
}
