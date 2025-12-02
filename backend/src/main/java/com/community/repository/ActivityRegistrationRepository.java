package com.community.repository;

import com.community.entity.ActivityRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动报名数据访问层
 */
@Repository
public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {
    
    Optional<ActivityRegistration> findByUserIdAndActivityId(Long userId, Long activityId);
    
    boolean existsByUserIdAndActivityId(Long userId, Long activityId);
    
    List<ActivityRegistration> findByUserId(Long userId);
    
    List<ActivityRegistration> findByActivityId(Long activityId);
    
    Page<ActivityRegistration> findByActivityIdAndStatus(Long activityId, 
                                                          ActivityRegistration.RegistrationStatus status, 
                                                          Pageable pageable);
    
    @Query("SELECT ar FROM ActivityRegistration ar WHERE ar.user.id = :userId " +
           "ORDER BY ar.registeredAt DESC")
    Page<ActivityRegistration> findUserActivityHistory(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT ar FROM ActivityRegistration ar WHERE ar.user.id = :userId AND " +
           "ar.status = 'REGISTERED' AND ar.activity.status = 'PUBLISHED'")
    List<ActivityRegistration> findUserUpcomingActivities(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ar) FROM ActivityRegistration ar WHERE ar.activity.id = :activityId AND ar.status = 'REGISTERED'")
    long countRegisteredByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT COUNT(ar) FROM ActivityRegistration ar WHERE ar.activity.id = :activityId AND ar.checkedIn = true")
    long countCheckedInByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT ar.status, COUNT(ar) FROM ActivityRegistration ar WHERE ar.activity.id = :activityId GROUP BY ar.status")
    List<Object[]> countRegistrationsByStatus(@Param("activityId") Long activityId);
}
