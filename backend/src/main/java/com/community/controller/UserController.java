package com.community.controller;

import com.community.dto.ApiResponse;
import com.community.dto.AuthResponse;
import com.community.entity.User;
import com.community.service.AuthService;
import com.community.service.FileUploadService;
import com.community.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
    private final FileUploadService fileUploadService;
    
    @GetMapping("/profile")
    @Operation(summary = "获取个人资料", description = "获取当前用户的个人资料和统计数据")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> getProfile() {
        User currentUser = authService.getCurrentUser();
        AuthResponse.UserDTO userDTO = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }
    
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
            @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        User currentUser = authService.getCurrentUser();
        userService.changePassword(currentUser.getId(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传当前用户头像")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) throws IOException {
        User currentUser = authService.getCurrentUser();
        String avatarUrl = fileUploadService.uploadFile(file, "avatars");
        AuthResponse.UserDTO updated = userService.updateUser(currentUser.getId(),
                AuthResponse.UserDTO.builder().avatar(avatarUrl).build());
        return ResponseEntity.ok(ApiResponse.success("头像上传成功", avatarUrl));
    }
    
    // ==================== 管理员接口 ====================
    
    @GetMapping("/admin/list")
    @Operation(summary = "获取所有用户", description = "管理员获取所有用户列表（分页）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuthResponse.UserDTO>>> getAllUsers(Pageable pageable) {
        Page<AuthResponse.UserDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @GetMapping("/admin/search")
    @Operation(summary = "管理员搜索用户", description = "管理员根据多条件搜索用户")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuthResponse.UserDTO>>> searchUsersAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable) {
        Page<AuthResponse.UserDTO> users = userService.searchUsersAdmin(keyword, role, enabled, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @PostMapping("/admin/create")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> createUser(
            @Valid @RequestBody AuthResponse.UserDTO userData,
            @RequestParam String password) {
        AuthResponse.UserDTO user = userService.createUser(userData, password);
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
    }
    
    @PutMapping("/admin/{id}")
    @Operation(summary = "管理员更新用户", description = "管理员更新指定用户信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthResponse.UserDTO>> updateUserByAdmin(
            @PathVariable Long id,
            @RequestBody AuthResponse.UserDTO updateData) {
        AuthResponse.UserDTO updated = userService.updateUserByAdmin(id, updateData);
        return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", updated));
    }
    
    @PutMapping("/admin/{id}/role")
    @Operation(summary = "更新用户角色", description = "管理员更新用户角色")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long id,
            @RequestParam User.UserRole role) {
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功"));
    }
    
    @PutMapping("/admin/{id}/status")
    @Operation(summary = "更新用户状态", description = "管理员启用/禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(enabled ? "用户已启用" : "用户已禁用"));
    }
    
    @PutMapping("/admin/{id}/password")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
    }
    
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除用户", description = "管理员删除用户（逻辑删除）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
    }
}
