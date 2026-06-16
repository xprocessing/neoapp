package com.neoapp.controller;

import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.service.CosService;
import com.neoapp.service.UserService;
import com.neoapp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CosService cosService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        return Result.success(userService.getUserInfo(userId));
    }

    /**
     * 用户自助更新个人资料
     */
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserProfileRequest request) {
        Long userId = getUserId(authHeader);
        return Result.success(userService.updateProfile(userId, request));
    }

    /**
     * 上传头像图片到 COS
     */
    @PostMapping("/upload/avatar")
    public Result<String> uploadAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        Long userId = getUserId(authHeader);
        String url = cosService.uploadAvatar(file, userId);
        return Result.success("上传成功", url);
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
