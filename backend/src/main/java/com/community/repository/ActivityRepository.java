package com.community.repository;

import com.community.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动数据访问层
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    Page<Activity> findByClubId(Long clubId, Pageable pageable);
    
    Page<Activity> findByClubIdAndStatus(Long clubId, Activity.ActivityStatus status, Pageable pageable);
    
    Page<Activity> findByOrganizerId(Long organizerId, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE " +
           "(:keyword IS NULL OR a.title LIKE %:keyword% OR a.description LIKE %:keyword%) AND " +
           "(:clubId IS NULL OR a.club.id = :clubId) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Activity> searchActivities(@Param("keyword") String keyword,
                                    @Param("clubId") Long clubId,
                                    @Param("status") Activity.ActivityStatus status,
                                    Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE " +
           "(:keyword IS NULL OR a.title LIKE %:keyword% OR a.description LIKE %:keyword%) AND " +
           "(:clubId IS NULL OR a.club.id = :clubId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:approvalStatus IS NULL OR a.approvalStatus = :approvalStatus)")
    Page<Activity> searchActivitiesAdmin(@Param("keyword") String keyword,
                                          @Param("clubId") Long clubId,
                                          @Param("status") Activity.ActivityStatus status,
                                          @Param("approvalStatus") Activity.ApprovalStatus approvalStatus,
                                          Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.approvalStatus = 'PENDING' ORDER BY a.createdAt DESC")
    Page<Activity> findPendingActivities(Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' AND a.approvalStatus = 'APPROVED' " +
           "AND a.registrationDeadline > :now ORDER BY a.startTime ASC")
    Page<Activity> findUpcomingActivities(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.club.id = :clubId AND a.status = 'COMPLETED' " +
           "ORDER BY a.endTime DESC")
    List<Activity> findCompletedActivitiesByClub(@Param("clubId") Long clubId, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.startTime BETWEEN :start AND :end")
    List<Activity> findActivitiesBetweenDates(@Param("start") LocalDateTime start, 
                                               @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' AND a.approvalStatus = 'APPROVED' " +
           "AND a.startTime BETWEEN :start AND :end")
    List<Activity> findPublishedActivitiesBetweenDates(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.club.id = :clubId AND a.status = 'COMPLETED'")
    long countCompletedActivitiesByClub(@Param("clubId") Long clubId);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.status = 'COMPLETED' AND " +
           "a.endTime BETWEEN :start AND :end")
    long countCompletedActivitiesBetweenDates(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.approvalStatus = 'PENDING'")
    long countPendingActivities();

       @Query("SELECT COUNT(a) FROM Activity a WHERE a.status IN ('PUBLISHED', 'ONGOING', 'COMPLETED') AND a.approvalStatus = 'APPROVED'")
       long countPublicActivities();
    
    @Query("SELECT a.club.type, COUNT(a) FROM Activity a WHERE a.status = 'COMPLETED' GROUP BY a.club.type")
    List<Object[]> countActivitiesByClubType();
}
