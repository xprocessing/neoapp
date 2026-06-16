package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.entity.*;
import com.neoapp.service.*;
import com.neoapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class InviteController {

    private final InviteUserService inviteUserService;
    private final InviteRewardLogService inviteRewardLogService;
    private final UserInfoService userInfoService;
    private final JwtUtil jwtUtil;

    // ======================== 管理端 ========================

    @GetMapping("/api/admin/invites")
    public Result<Page<InviteUserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 搜索用户
        LambdaQueryWrapper<UserInfo> uWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            uWrapper.and(w -> w.like(UserInfo::getNickname, keyword)
                .or().like(UserInfo::getPhone, keyword));
        }
        List<UserInfo> users = userInfoService.list(uWrapper);
        Map<Long, UserInfo> userMap = users.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));

        LambdaQueryWrapper<InviteUser> wrapper = new LambdaQueryWrapper<>();
        if (!users.isEmpty()) {
            wrapper.in(InviteUser::getUserId, users.stream().map(UserInfo::getId).toList());
        } else if (keyword != null && !keyword.isBlank()) {
            return Result.success(new Page<>(page, size));
        }
        wrapper.orderByDesc(InviteUser::getInviteNum);
        Page<InviteUser> p = inviteUserService.page(new Page<>(page, size), wrapper);

        // 查上级信息
        Set<Long> parentIds = p.getRecords().stream().map(InviteUser::getParentId).filter(pid -> pid > 0).collect(Collectors.toSet());
        Map<Long, UserInfo> parentMap = Collections.emptyMap();
        if (!parentIds.isEmpty()) {
            parentMap = userInfoService.listByIds(parentIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, u -> u));
        }

        Page<InviteUserVO> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent()); voPage.setSize(p.getSize()); voPage.setTotal(p.getTotal());
        Map<Long, UserInfo> finalParentMap = parentMap;
        voPage.setRecords(p.getRecords().stream().map(iu -> {
            InviteUserVO vo = new InviteUserVO();
            vo.setUserId(iu.getUserId());
            vo.setParentId(iu.getParentId());
            vo.setInviteNum(iu.getInviteNum());
            vo.setCreateTime(iu.getCreateTime());
            UserInfo ui = userMap.get(iu.getUserId());
            if (ui != null) { vo.setNickname(ui.getNickname()); vo.setPhone(ui.getPhone()); }
            UserInfo pi = finalParentMap.get(iu.getParentId());
            if (pi != null) vo.setParentNickname(pi.getNickname());
            return vo;
        }).toList());
        return Result.success(voPage);
    }

    @GetMapping("/api/admin/invites/rewards")
    public Result<Page<InviteRewardVO>> rewards(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {

        LambdaQueryWrapper<InviteRewardLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(InviteRewardLog::getUserId, userId);
        wrapper.orderByDesc(InviteRewardLog::getCreateTime);
        Page<InviteRewardLog> p = inviteRewardLogService.page(new Page<>(page, size), wrapper);

        Set<Long> allIds = new HashSet<>();
        p.getRecords().forEach(r -> { allIds.add(r.getUserId()); allIds.add(r.getInviteUserId()); });
        Map<Long, UserInfo> userMap = userInfoService.listByIds(allIds).stream()
            .collect(Collectors.toMap(UserInfo::getId, u -> u));

        Page<InviteRewardVO> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent()); voPage.setSize(p.getSize()); voPage.setTotal(p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(r -> {
            InviteRewardVO vo = new InviteRewardVO();
            vo.setId(r.getId()); vo.setUserId(r.getUserId());
            vo.setInviteUserId(r.getInviteUserId());
            vo.setRewardType(r.getRewardType()); vo.setRewardNum(r.getRewardNum());
            vo.setRemark(r.getRemark()); vo.setCreateTime(r.getCreateTime());
            UserInfo u = userMap.get(r.getUserId()); if (u != null) vo.setUserNickname(u.getNickname());
            UserInfo iu = userMap.get(r.getInviteUserId()); if (iu != null) vo.setInviteUserNickname(iu.getNickname());
            return vo;
        }).toList());
        return Result.success(voPage);
    }

    // ======================== C 端 ========================

    @GetMapping("/api/user/invites")
    public Result<Map<String, Object>> myInvites(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        InviteUser iu = inviteUserService.getOne(
            new LambdaQueryWrapper<InviteUser>().eq(InviteUser::getUserId, userId));
        UserInfo ui = userInfoService.getById(userId);
        return Result.success(Map.of(
            "inviteCode", ui != null ? ui.getInviteCode() : "",
            "inviteNum", iu != null ? iu.getInviteNum() : 0
        ));
    }

    private Long getUserId(String authHeader) {
        return jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
    }
}
