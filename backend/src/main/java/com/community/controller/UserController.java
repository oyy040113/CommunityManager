package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.dto.AuthResponse;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {
    
    private final UserService userService;
    private final AuthService authService;
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据ID获取用户详细信息")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(AuthResponse.UserDTO.fromEntity(user)));
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键字搜索用户")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuthResponse.UserDTO>>> searchUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<AuthResponse.UserDTO> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息", description = "更新当前用户的个人信息")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> updateProfile(
            @RequestBody AuthResponse.UserDTO updateData) {
        User currentUser = authService.getCurrentUser();
        AuthResponse.UserDTO updated = userService.updateUser(currentUser.getId(), updateData);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
    }
    
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        User currentUser = authService.getCurrentUser();
        userService.changePassword(currentUser.getId(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }
    
    @PutMapping("/{id}/role")
    @Operation(summary = "更新用户角色", description = "管理员更新用户角色")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long id,
            @RequestParam User.UserRole role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功"));
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "管理员启用/禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(enabled ? "用户已启用" : "用户已禁用"));
    }
}
