package com.community.repository;

import com.community.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByStudentId(String studentId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentId(String studentId);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.studentId LIKE %:keyword%)")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.realName LIKE %:keyword% OR u.studentId LIKE %:keyword%) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsersAdmin(@Param("keyword") String keyword, 
                                @Param("role") User.UserRole role,
                                @Param("enabled") Boolean enabled,
                                Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findByRole(@Param("role") User.UserRole role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
}
