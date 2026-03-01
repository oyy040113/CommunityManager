package com.community.service;

import com.community.dto.AuthResponse;
import com.community.entity.User;
import com.community.exception.BusinessException;
import com.community.repository.ActivityRegistrationRepository;
import com.community.repository.ClubMemberRepository;
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
    private final ClubMemberRepository clubMemberRepository;
    private final ActivityRegistrationRepository activityRegistrationRepository;
    
    /**
     * 根据ID获取用户资料（包含统计信息）
     */
    public AuthResponse.UserDTO getUserProfile(Long userId) {
        User user = getUserById(userId);
        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.fromEntity(user);
        
        // 获取社团数量
        int clubCount = (int) clubMemberRepository.findApprovedMembershipsByUserId(userId).size();
        userDTO.setClubCount(clubCount);
        
        // 获取参与活动数量
        int activityCount = activityRegistrationRepository.findByUserId(userId).size();
        userDTO.setActivityCount(activityCount);
        
        // 获取评价数量（暂时设为0，可根据需要完善）
        userDTO.setFeedbackCount(0);
        
        return userDTO;
    }
    
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
     * 管理员搜索用户（支持更多筛选条件）
     */
    public Page<AuthResponse.UserDTO> searchUsersAdmin(String keyword, User.UserRole role, 
                                                        Boolean enabled, Pageable pageable) {
        return userRepository.searchUsersAdmin(keyword, role, enabled, pageable)
                .map(AuthResponse.UserDTO::fromEntity);
    }
    
    /**
     * 获取所有用户（分页）
     */
    public Page<AuthResponse.UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(AuthResponse.UserDTO::fromEntity);
    }
    
    /**
     * 创建用户（管理员使用）
     */
    @Transactional
    public AuthResponse.UserDTO createUser(AuthResponse.UserDTO userData, String password) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userData.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userData.getEmail() != null && userRepository.existsByEmail(userData.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 检查学号是否已存在
        if (userData.getStudentId() != null && userRepository.existsByStudentId(userData.getStudentId())) {
            throw new BusinessException("学号已被使用");
        }
        
        User user = User.builder()
                .username(userData.getUsername())
                .password(passwordEncoder.encode(password))
                .realName(userData.getRealName())
                .studentId(userData.getStudentId())
                .email(userData.getEmail())
                .phone(userData.getPhone())
                .avatar(userData.getAvatar())
                .department(userData.getDepartment())
                .major(userData.getMajor())
                .grade(userData.getGrade())
                .role(userData.getRole() != null ? userData.getRole() : User.UserRole.USER)
                .enabled(true)
                .build();
        
        user = userRepository.save(user);
        log.info("管理员创建用户成功: {}", user.getUsername());
        
        return AuthResponse.UserDTO.fromEntity(user);
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
     * 管理员更新用户信息（可以更新更多字段）
     */
    @Transactional
    public AuthResponse.UserDTO updateUserByAdmin(Long userId, AuthResponse.UserDTO updateData) {
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
        if (updateData.getStudentId() != null) {
            if (!updateData.getStudentId().equals(user.getStudentId()) && 
                userRepository.existsByStudentId(updateData.getStudentId())) {
                throw new BusinessException("学号已被使用");
            }
            user.setStudentId(updateData.getStudentId());
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
        if (updateData.getRole() != null) {
            user.setRole(updateData.getRole());
        }
        
        user = userRepository.save(user);
        log.info("管理员更新用户信息: {}", user.getUsername());
        
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
     * 管理员重置用户密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("管理员重置用户密码: {}", user.getUsername());
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
     * 删除用户（管理员使用）
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        
        // 检查是否是管理员
        if (user.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("不能删除管理员账户");
        }
        
        // 逻辑删除：禁用用户
        user.setEnabled(false);
        userRepository.save(user);
        log.info("用户已删除（禁用）: {}", user.getUsername());
    }
    
    /**
     * 统计活跃用户数
     */
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }
    
    /**
     * 按角色统计用户数
     */
    public long countByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }
}
