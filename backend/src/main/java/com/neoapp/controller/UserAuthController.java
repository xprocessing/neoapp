package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.entity.AdminUser;
import com.neoapp.service.AdminUserService;
import com.neoapp.service.UserService;
import com.neoapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;
    private final AdminUserService adminUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    /**
     * C 端用户登录（手机号）
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest request,
                                HttpServletRequest httpRequest) {
        String token = userService.login(request, getClientIp(httpRequest));
        return Result.success("登录成功", token);
    }

    /**
     * 后台管理员登录（用户名 + 密码）
     */
    @PostMapping("/admin/login")
    public Result<String> adminLogin(@Valid @RequestBody AdminLoginRequest request,
                                     HttpServletRequest httpRequest) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, request.getUsername());
        AdminUser admin = adminUserService.getOne(wrapper);

        if (admin == null) {
            return Result.badRequest("用户名或密码错误");
        }
        if (admin.getStatus() != 1) {
            return Result.forbidden("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return Result.badRequest("用户名或密码错误");
        }

        // 更新最后登录时间和IP
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(getClientIp(httpRequest));
        adminUserService.updateById(admin);

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());
        return Result.success("登录成功", token);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
