package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.entity.AdminUser;
import com.neoapp.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理员管理 —— 管理 admin_user 表
 */
@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminUserService adminUserService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询管理员列表
     */
    @GetMapping
    public Result<Page<AdminUserVO>> list(AdminUserPageQuery query) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                .like(AdminUser::getUsername, query.getKeyword())
                .or()
                .like(AdminUser::getNickname, query.getKeyword())
                .or()
                .like(AdminUser::getPhone, query.getKeyword())
            );
        }
        if (query.getStatus() != null) {
            wrapper.eq(AdminUser::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(AdminUser::getCreateTime);

        Page<AdminUser> page = adminUserService.page(
            new Page<>(query.getPage(), query.getSize()), wrapper
        );

        Page<AdminUserVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());

        return Result.success(voPage);
    }

    /**
     * 查询单个管理员详情
     */
    @GetMapping("/{id}")
    public Result<AdminUserVO> detail(@PathVariable Long id) {
        AdminUser user = adminUserService.getById(id);
        if (user == null) {
            return Result.notFound("管理员不存在");
        }
        return Result.success(toVO(user));
    }

    /**
     * 新增管理员
     */
    @PostMapping
    public Result<AdminUserVO> create(@RequestBody AdminUserRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            return Result.badRequest("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            return Result.badRequest("密码不能为空");
        }

        // 检查用户名唯一
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminUser::getUsername, request.getUsername());
        if (adminUserService.count(wrapper) > 0) {
            return Result.badRequest("用户名已存在");
        }

        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        adminUserService.save(user);
        return Result.success("创建成功", toVO(user));
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{id}")
    public Result<AdminUserVO> update(@PathVariable Long id, @RequestBody AdminUserRequest request) {
        AdminUser user = adminUserService.getById(id);
        if (user == null) {
            return Result.notFound("管理员不存在");
        }

        if (StringUtils.hasText(request.getUsername())) {
            // 检查用户名唯一（排除自身）
            LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdminUser::getUsername, request.getUsername());
            wrapper.ne(AdminUser::getId, id);
            if (adminUserService.count(wrapper) > 0) {
                return Result.badRequest("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        adminUserService.updateById(user);
        return Result.success("更新成功", toVO(user));
    }

    /**
     * 删除管理员（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AdminUser user = adminUserService.getById(id);
        if (user == null) {
            return Result.notFound("管理员不存在");
        }
        // 不允许删除自己或超级管理员
        if ("admin".equals(user.getUsername())) {
            return Result.forbidden("不能删除超级管理员");
        }
        adminUserService.removeById(id);
        return Result.success();
    }

    private AdminUserVO toVO(AdminUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
