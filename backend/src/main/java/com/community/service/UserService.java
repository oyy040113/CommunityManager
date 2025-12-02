package com.community.service;

import com.community.dto.AuthResponse;
import com.community.entity.User;
import com.community.exception.BusinessException;
import com.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 搜索用户
     */
    public Page<AuthResponse.UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable)
                .map(AuthResponse.UserDTO::fromEntity);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public AuthResponse.UserDTO updateUser(Long userId, AuthResponse.UserDTO updateData) {
        User user = getUserById(userId);
        
        if (updateData.getRealName() != null) {
            user.setRealName(updateData.getRealName());
        }
        if (updateData.getEmail() != null) {
            if (!updateData.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(updateData.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(updateData.getEmail());
        }
        if (updateData.getPhone() != null) {
            user.setPhone(updateData.getPhone());
        }
        if (updateData.getAvatar() != null) {
            user.setAvatar(updateData.getAvatar());
        }
        if (updateData.getDepartment() != null) {
            user.setDepartment(updateData.getDepartment());
        }
        if (updateData.getMajor() != null) {
            user.setMajor(updateData.getMajor());
        }
        if (updateData.getGrade() != null) {
            user.setGrade(updateData.getGrade());
        }
        
        user = userRepository.save(user);
        log.info("用户信息更新成功: {}", user.getUsername());
        
        return AuthResponse.UserDTO.fromEntity(user);
    }
    
    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户密码修改成功: {}", user.getUsername());
    }
    
    /**
     * 更新用户角色（仅管理员）
     */
    @Transactional
    public void updateUserRole(Long userId, User.UserRole role) {
        User user = getUserById(userId);
        user.setRole(role);
        userRepository.save(user);
        log.info("用户角色更新: {} -> {}", user.getUsername(), role);
    }
    
    /**
     * 禁用/启用用户
     */
    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        User user = getUserById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("用户状态更新: {} -> {}", user.getUsername(), enabled ? "启用" : "禁用");
    }
    
    /**
     * 统计活跃用户数
     */
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
}
