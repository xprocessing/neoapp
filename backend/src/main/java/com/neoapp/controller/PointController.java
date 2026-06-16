package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.Result;
import com.neoapp.dto.*;
import com.neoapp.entity.*;
import com.neoapp.service.*;
import com.neoapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointUserService pointUserService;
    private final PointLogService pointLogService;
    private final UserInfoService userInfoService;
    private final JwtUtil jwtUtil;

    // ======================== 管理端 ========================

    /**
     * 用户积分列表
     */
    @GetMapping("/api/admin/points")
    public Result<Page<PointUserVO>> adminPointList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 先查用户信息
        LambdaQueryWrapper<UserInfo> uWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            uWrapper.and(w -> w.like(UserInfo::getNickname, keyword)
                .or().like(UserInfo::getPhone, keyword));
        }
        List<UserInfo> users = userInfoService.list(uWrapper);
        Map<Long, UserInfo> userMap = users.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
        List<Long> userIds = users.stream().map(UserInfo::getId).toList();

        // 查积分
        LambdaQueryWrapper<PointUser> wrapper = new LambdaQueryWrapper<>();
        if (!userIds.isEmpty()) {
            wrapper.in(PointUser::getUserId, userIds);
        } else if (StringUtils.hasText(keyword)) {
            return Result.success(new Page<>(page, size));
        }
        wrapper.orderByDesc(PointUser::getUsablePoint);
        Page<PointUser> p = pointUserService.page(new Page<>(page, size), wrapper);

        Page<PointUserVO> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent());
        voPage.setSize(p.getSize());
        voPage.setTotal(p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(pu -> {
            PointUserVO vo = new PointUserVO();
            vo.setId(pu.getId());
            vo.setUserId(pu.getUserId());
            vo.setTotalPoint(pu.getTotalPoint());
            vo.setUsablePoint(pu.getUsablePoint());
            vo.setUpdateTime(pu.getUpdateTime());
            UserInfo ui = userMap.get(pu.getUserId());
            if (ui != null) {
                vo.setNickname(ui.getNickname());
                vo.setPhone(ui.getPhone());
            }
            return vo;
        }).toList());
        return Result.success(voPage);
    }

    /**
     * 积分流水列表（可按用户筛选）
     */
    @GetMapping("/api/admin/points/logs")
    public Result<Page<PointLogVO>> adminPointLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {

        LambdaQueryWrapper<PointLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(PointLog::getUserId, userId);
        if (type != null) wrapper.eq(PointLog::getType, type);
        wrapper.orderByDesc(PointLog::getCreateTime);
        Page<PointLog> p = pointLogService.page(new Page<>(page, size), wrapper);

        // 批量查用户信息
        List<Long> uids = p.getRecords().stream().map(PointLog::getUserId).distinct().toList();
        Map<Long, UserInfo> userMap = userInfoService.listByIds(uids).stream()
            .collect(Collectors.toMap(UserInfo::getId, u -> u));

        Page<PointLogVO> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent());
        voPage.setSize(p.getSize());
        voPage.setTotal(p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(pl -> {
            PointLogVO vo = new PointLogVO();
            vo.setId(pl.getId());
            vo.setUserId(pl.getUserId());
            vo.setPoint(pl.getPoint());
            vo.setBalance(pl.getBalance());
            vo.setType(pl.getType());
            vo.setSource(pl.getSource());
            vo.setRemark(pl.getRemark());
            vo.setCreateTime(pl.getCreateTime());
            UserInfo ui = userMap.get(pl.getUserId());
            if (ui != null) { vo.setNickname(ui.getNickname()); vo.setPhone(ui.getPhone()); }
            return vo;
        }).toList());
        return Result.success(voPage);
    }

    // ======================== C 端 ========================

    /**
     * 当前用户积分
     */
    @GetMapping("/api/user/points")
    public Result<PointUserVO> myPoints(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        PointUser pu = pointUserService.getOne(
            new LambdaQueryWrapper<PointUser>().eq(PointUser::getUserId, userId));
        if (pu == null) {
            PointUserVO vo = new PointUserVO();
            vo.setTotalPoint(0);
            vo.setUsablePoint(0);
            return Result.success(vo);
        }
        PointUserVO vo = new PointUserVO();
        vo.setUserId(pu.getUserId());
        vo.setTotalPoint(pu.getTotalPoint());
        vo.setUsablePoint(pu.getUsablePoint());
        vo.setUpdateTime(pu.getUpdateTime());
        return Result.success(vo);
    }

    /**
     * 当前用户积分流水
     */
    @GetMapping("/api/user/points/logs")
    public Result<Page<PointLog>> myPointLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = getUserId(authHeader);
        LambdaQueryWrapper<PointLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointLog::getUserId, userId);
        wrapper.orderByDesc(PointLog::getCreateTime);
        return Result.success(pointLogService.page(new Page<>(page, size), wrapper));
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
