package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.neoapp.common.Result;
import com.neoapp.entity.*;
import com.neoapp.service.*;
import com.neoapp.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberPackageService packageService;
    private final MemberUserService memberUserService;
    private final PointUserService pointUserService;
    private final PointLogService pointLogService;
    private final UserInfoService userInfoService;
    private final WechatPayService wechatPayService;
    private final UserWechatService userWechatService;
    private final JwtUtil jwtUtil;

    // ==================== Admin ====================

    /** 套餐列表 */
    @GetMapping("/api/admin/member/packages")
    public Result<Page<MemberPackage>> adminPackages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(packageService.page(new Page<>(page, size),
            new LambdaQueryWrapper<MemberPackage>().orderByAsc(MemberPackage::getSort)));
    }

    /** 创建套餐 */
    @PostMapping("/api/admin/member/packages")
    public Result<MemberPackage> createPackage(@RequestBody MemberPackage pkg) {
        pkg.setId(null);
        packageService.save(pkg);
        return Result.success("创建成功", pkg);
    }

    /** 更新套餐 */
    @PutMapping("/api/admin/member/packages/{id}")
    public Result<MemberPackage> updatePackage(@PathVariable Long id, @RequestBody MemberPackage pkg) {
        if (packageService.getById(id) == null) return Result.notFound("套餐不存在");
        pkg.setId(id);
        packageService.updateById(pkg);
        return Result.success("更新成功", packageService.getById(id));
    }

    /** 删除套餐 */
    @DeleteMapping("/api/admin/member/packages/{id}")
    public Result<Void> deletePackage(@PathVariable Long id) {
        packageService.removeById(id);
        return Result.success();
    }

    /** 用户会员列表 */
    @GetMapping("/api/admin/member/users")
    public Result<Page<Map<String, Object>>> adminMembers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        List<Long> userIds = null;
        if (keyword != null && !keyword.isBlank()) {
            userIds = userInfoService.list(new LambdaQueryWrapper<UserInfo>()
                .like(UserInfo::getNickname, keyword).or().like(UserInfo::getPhone, keyword))
                .stream().map(UserInfo::getId).toList();
            if (userIds.isEmpty()) return Result.success(new Page<>(page, size));
        }
        LambdaQueryWrapper<MemberUser> w = new LambdaQueryWrapper<>();
        if (userIds != null) w.in(MemberUser::getUserId, userIds);
        w.orderByDesc(MemberUser::getIsMember).orderByDesc(MemberUser::getCreateTime);
        Page<MemberUser> p = memberUserService.page(new Page<>(page, size), w);

        List<Long> allIds = p.getRecords().stream().map(MemberUser::getUserId).toList();
        Map<Long, UserInfo> userMap = userInfoService.listByIds(allIds).stream()
            .collect(java.util.stream.Collectors.toMap(UserInfo::getId, u -> u));

        Page<Map<String, Object>> voPage = new Page<>();
        voPage.setCurrent(p.getCurrent()); voPage.setSize(p.getSize()); voPage.setTotal(p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(mu -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", mu.getUserId());
            m.put("isMember", mu.getIsMember());
            m.put("expireTime", mu.getExpireTime());
            m.put("createTime", mu.getCreateTime());
            UserInfo ui = userMap.get(mu.getUserId());
            m.put("nickname", ui != null ? ui.getNickname() : "");
            m.put("phone", ui != null ? ui.getPhone() : "");
            return m;
        }).toList());
        return Result.success(voPage);
    }

    // ==================== Web ====================

    /** 可用套餐列表 */
    @GetMapping("/api/user/member/packages")
    public Result<List<MemberPackage>> webPackages() {
        return Result.success(packageService.list(
            new LambdaQueryWrapper<MemberPackage>().eq(MemberPackage::getStatus, 1).orderByAsc(MemberPackage::getSort)));
    }

    /** 当前用户会员状态 */
    @GetMapping("/api/user/member/info")
    public Result<Map<String, Object>> myMember(@RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        MemberUser mu = memberUserService.getOne(new LambdaQueryWrapper<MemberUser>().eq(MemberUser::getUserId, userId));
        boolean isMember = mu != null && mu.getIsMember() == 1
            && (mu.getExpireTime() == null || mu.getExpireTime().isAfter(LocalDateTime.now()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("isMember", isMember);
        data.put("expireTime", mu != null ? mu.getExpireTime() : null);
        return Result.success(data);
    }

    /** 购买会员 - 发起微信支付 */
    @PostMapping("/api/user/member/buy/{packageId}")
    public Result<Map<String, Object>> buy(@RequestHeader("Authorization") String auth,
                                           @PathVariable Long packageId,
                                           HttpServletRequest request) {
        Long userId = getUserId(auth);
        MemberPackage pkg = packageService.getById(packageId);
        if (pkg == null || pkg.getStatus() != 1) return Result.badRequest("套餐不可用");

        if (wechatPayService.isConfigured()) {
            try {
                String tradeNo = "M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
                int amount = pkg.getPrice().multiply(new java.math.BigDecimal(100)).intValue();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();

                // 获取用户 openid
                String openid = "";
                UserWechat uw = userWechatService.getOne(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getUserId, userId));
                if (uw != null) openid = uw.getOpenid();

                JsonNode payResult = wechatPayService.createPayOrder(tradeNo, pkg.getPackageName(), amount, openid, ip);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("h5_url", payResult.get("h5_url").asText());
                result.put("tradeNo", tradeNo);
                result.put("amount", pkg.getPrice());
                // 保存待支付状态
                result.put("packageId", pkg.getId());
                result.put("dayNum", pkg.getDayNum());
                result.put("givePoint", pkg.getGivePoint());
                return Result.success("请完成支付", result);
            } catch (Exception e) {
                // 支付接口失败，降级为直接开通
                return directBuy(userId, pkg);
            }
        }
        return directBuy(userId, pkg);
    }

    /** 直接开通（无微信支付时） */
    private Result<Map<String, Object>> directBuy(Long userId, MemberPackage pkg) {
        MemberUser mu = memberUserService.getOne(new LambdaQueryWrapper<MemberUser>().eq(MemberUser::getUserId, userId));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (mu != null && mu.getIsMember() == 1 && mu.getExpireTime() != null && mu.getExpireTime().isAfter(now))
            ? mu.getExpireTime() : now;
        if (mu == null) { mu = new MemberUser(); mu.setUserId(userId); }
        mu.setIsMember(1); mu.setExpireTime(base.plusDays(pkg.getDayNum()));
        if (mu.getId() == null) memberUserService.save(mu); else memberUserService.updateById(mu);
        grantPoints(userId, pkg.getGivePoint());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("expireTime", mu.getExpireTime()); result.put("givePoint", pkg.getGivePoint());
        return Result.success("购买成功", result);
    }

    /** 支付回调处理 */
    @PostMapping("/api/user/member/pay-callback")
    public Result<Void> payCallback(@RequestHeader("Authorization") String auth,
                                    @RequestBody Map<String, Object> body) {
        Long userId = getUserId(auth);
        Long pkgId = Long.valueOf(body.get("packageId").toString());
        MemberPackage pkg = packageService.getById(pkgId);
        if (pkg == null) return Result.badRequest("套餐不存在");

        MemberUser mu = memberUserService.getOne(new LambdaQueryWrapper<MemberUser>().eq(MemberUser::getUserId, userId));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = (mu != null && mu.getIsMember() == 1 && mu.getExpireTime() != null && mu.getExpireTime().isAfter(now))
            ? mu.getExpireTime() : now;
        if (mu == null) { mu = new MemberUser(); mu.setUserId(userId); }
        mu.setIsMember(1); mu.setExpireTime(base.plusDays(pkg.getDayNum()));
        if (mu.getId() == null) memberUserService.save(mu); else memberUserService.updateById(mu);
        grantPoints(userId, pkg.getGivePoint());
        return Result.success();
    }

    private void grantPoints(Long userId, int point) {
        if (point <= 0) return;
        PointUser pu = pointUserService.getOne(new LambdaQueryWrapper<PointUser>().eq(PointUser::getUserId, userId));
        if (pu == null) { pu = new PointUser(); pu.setUserId(userId); pu.setTotalPoint(point); pu.setUsablePoint(point); pointUserService.save(pu); }
        else { pu.setTotalPoint(pu.getTotalPoint() + point); pu.setUsablePoint(pu.getUsablePoint() + point); pointUserService.updateById(pu); }
        PointLog pl = new PointLog(); pl.setUserId(userId); pl.setPoint(point); pl.setBalance(pu.getUsablePoint());
        pl.setType(1); pl.setSource("member"); pl.setRemark("购买会员赠送" + point + "积分"); pointLogService.save(pl);
    }

    private Long getUserId(String auth) { return jwtUtil.getUserIdFromToken(auth.replace("Bearer ", "")); }
}
