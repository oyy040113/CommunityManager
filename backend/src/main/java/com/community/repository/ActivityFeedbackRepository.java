package com.community.repository;

import com.community.entity.ActivityFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动反馈数据访问层
 */
@Repository
public interface ActivityFeedbackRepository extends JpaRepository<ActivityFeedback, Long> {
    
    Optional<ActivityFeedback> findByUserIdAndActivityId(Long userId, Long activityId);
    
    boolean existsByUserIdAndActivityId(Long userId, Long activityId);
    
    List<ActivityFeedback> findByActivityId(Long activityId);
    
    Page<ActivityFeedback> findByActivityIdOrderByCreatedAtDesc(Long activityId, Pageable pageable);
    
    @Query("SELECT AVG(af.rating) FROM ActivityFeedback af WHERE af.activity.id = :activityId")
    Double getAverageRatingByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT COUNT(af) FROM ActivityFeedback af WHERE af.activity.id = :activityId")
    long countByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT af.rating, COUNT(af) FROM ActivityFeedback af WHERE af.activity.id = :activityId GROUP BY af.rating ORDER BY af.rating DESC")
    List<Object[]> getRatingDistributionByActivityId(@Param("activityId") Long activityId);

    Page<ActivityFeedback> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Query("SELECT AVG(af.rating) FROM ActivityFeedback af WHERE af.activity.club.id = :clubId")
    Double getAverageRatingByClubId(@Param("clubId") Long clubId);
    
    @Query("SELECT COUNT(af) FROM ActivityFeedback af WHERE af.activity.club.id = :clubId")
    long countByClubId(@Param("clubId") Long clubId);
    
    @Modifying
    @Query("DELETE FROM ActivityFeedback af WHERE af.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") Long activityId);
}
