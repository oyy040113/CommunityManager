package com.community.service;

import com.community.dto.AuthResponse;
import com.community.dto.LoginRequest;
import com.community.dto.RegisterRequest;
import com.community.entity.User;
import com.community.exception.BusinessException;
import com.community.repository.UserRepository;
import com.community.security.JwtTokenProvider;
import com.community.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已被使用");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 检查学号是否已存在
        if (request.getStudentId() != null && userRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号已被使用");
        }
        
        // 判断是否为第一个用户，如果是则设为管理员
        long totalUsers = userRepository.count();
        User.UserRole role = (totalUsers == 0) ? User.UserRole.ADMIN : User.UserRole.USER;
        
        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .studentId(request.getStudentId())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .major(request.getMajor())
                .grade(request.getGrade())
                .role(role)
                .enabled(true)
                .build();
        
        user = userRepository.save(user);
        log.info("用户注册成功: {} (角色: {})", user.getUsername(), role.name());
        
        // 生成token
        return generateAuthResponse(user);
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 更新最后登录时间
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("用户登录成功: {}", user.getUsername());
        
        return generateAuthResponse(user);
    }
    
    /**
     * 刷新Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期");
        }
        
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return generateAuthResponse(user);
    }
    
    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    /**
     * 生成认证响应
     */
    private AuthResponse generateAuthResponse(User user) {
        String token = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .user(AuthResponse.UserDTO.fromEntity(user))
                .build();
    }
}
