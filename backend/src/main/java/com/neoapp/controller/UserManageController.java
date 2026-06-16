package com.neoapp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManageController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<Page<UserInfoVO>> list(UserPageQuery query) {
        return Result.success(userService.getUserPage(query));
    }

    /**
     * 查询单个用户详情
     */
    @GetMapping("/{id}")
    public Result<UserInfoVO> detail(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<UserInfoVO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.success("更新成功", userService.updateUser(id, request));
    }

    /**
     * 删除用户（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
