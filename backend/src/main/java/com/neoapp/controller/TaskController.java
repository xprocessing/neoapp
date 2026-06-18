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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskInfoService taskInfoService;
    private final TaskUserOrderService orderService;
    private final TaskPayLogService payLogService;
    private final SysConfigService sysConfigService;
    private final WechatPayService wechatPayService;
    private final UserWechatService userWechatService;
    private final JwtUtil jwtUtil;

    // ==================== Admin ====================

    /** 任务列表 */
    @GetMapping("/api/admin/tasks")
    public Result<Page<TaskInfo>> adminList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<TaskInfo> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(TaskInfo::getTaskTitle, keyword);
        w.orderByDesc(TaskInfo::getSort).orderByDesc(TaskInfo::getCreateTime);
        return Result.success(taskInfoService.page(new Page<>(page, size), w));
    }

    /** 创建任务 */
    @PostMapping("/api/admin/tasks")
    public Result<TaskInfo> create(@RequestBody TaskInfo task) {
        task.setId(null);
        calcTotalFromHours(task);
        taskInfoService.save(task);
        return Result.success("创建成功", task);
    }

    /** 更新任务 */
    @PutMapping("/api/admin/tasks/{id}")
    public Result<TaskInfo> update(@PathVariable Long id, @RequestBody TaskInfo task) {
        TaskInfo exist = taskInfoService.getById(id);
        if (exist == null) return Result.notFound("任务不存在");
        task.setId(id);
        calcTotalFromHours(task);
        taskInfoService.updateById(task);
        return Result.success("更新成功", taskInfoService.getById(id));
    }

    /** 删除任务 */
    @DeleteMapping("/api/admin/tasks/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskInfoService.removeById(id);
        return Result.success();
    }

    /** 审核订单列表 */
    @GetMapping("/api/admin/tasks/orders")
    public Result<Page<TaskUserOrder>> adminOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer auditStatus) {
        LambdaQueryWrapper<TaskUserOrder> w = new LambdaQueryWrapper<>();
        if (auditStatus != null) w.eq(TaskUserOrder::getAuditStatus, auditStatus);
        w.orderByDesc(TaskUserOrder::getCreateTime);
        return Result.success(orderService.page(new Page<>(page, size), w));
    }

    /** 审核订单（通过/驳回） */
    @PutMapping("/api/admin/tasks/orders/{id}/audit")
    public Result<Void> auditOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TaskUserOrder order = orderService.getById(id);
        if (order == null) return Result.notFound("订单不存在");
        int status = (int) body.get("auditStatus");
        String note = (String) body.getOrDefault("auditNote", "");
        boolean grant = Boolean.TRUE.equals(body.get("grantPay"));
        order.setAuditStatus(status);
        order.setAuditNote(note);
        order.setAuditTime(LocalDateTime.now());
        if (grant) order.setGrantPay(1);
        orderService.updateById(order);
        return Result.success();
    }

    /** 授权打款 */
    @PutMapping("/api/admin/tasks/orders/{id}/grant")
    public Result<Void> grantPay(@PathVariable Long id) {
        TaskUserOrder order = orderService.getById(id);
        if (order == null) return Result.notFound("订单不存在");
        order.setGrantPay(1);
        orderService.updateById(order);
        return Result.success();
    }

    /** 打款日志 */
    @GetMapping("/api/admin/tasks/pay-logs")
    public Result<Page<TaskPayLog>> adminPayLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {
        return Result.success(payLogService.page(new Page<>(page, size),
            new LambdaQueryWrapper<TaskPayLog>().orderByDesc(TaskPayLog::getCreateTime)));
    }

    /** 重新打款（管理端手动触发） */
    @PostMapping("/api/admin/tasks/pay-logs/{id}/retry")
    public Result<String> retryPay(@PathVariable Long id) {
        TaskPayLog log = payLogService.getById(id);
        if (log == null) return Result.notFound("打款记录不存在");
        if (log.getPayStatus() == 2) return Result.badRequest("该笔已打款成功");
        if (!wechatPayService.isConfigured()) return Result.badRequest("微信支付未配置");

        // 获取用户 openid
        String openid = getOpenidByUserId(log.getUserId());
        if (openid == null || openid.isBlank()) return Result.badRequest("用户未绑定微信，无法打款");

        log.setRetryCount(log.getRetryCount() + 1);
        log.setPayStatus(1);
        try {
            JsonNode result = wechatPayService.transferToWallet(
                openid,
                log.getPayAmount().multiply(new java.math.BigDecimal(100)).intValue(),
                null, "任务奖励"
            );
            wechatPayService.updatePayLog(log, result);
            if (log.getPayStatus() == 2) {
                TaskUserOrder order = orderService.getById(log.getOrderId());
                if (order != null) { order.setWithdrawStatus(2); orderService.updateById(order); }
            }
            payLogService.updateById(log);
            return Result.success(log.getPayStatus() == 2 ? "打款成功" : "处理中");
        } catch (Exception e) {
            log.setFailReason(e.getMessage());
            payLogService.updateById(log);
            return Result.badRequest("打款失败: " + e.getMessage());
        }
    }

    /** 获取用户 openid */
    private String getOpenidByUserId(Long userId) {
        UserWechat uw = userWechatService.getOne(
            new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getUserId, userId));
        return uw != null ? uw.getOpenid() : "";
    }

    // ==================== Web (C端) ====================

    /** 上架任务列表（web端） */
    @GetMapping("/api/user/tasks")
    public Result<Page<TaskInfo>> webList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<TaskInfo> w = new LambdaQueryWrapper<>();
        w.eq(TaskInfo::getStatus, 1).orderByDesc(TaskInfo::getSort);
        return Result.success(taskInfoService.page(new Page<>(page, size), w));
    }

    /** 任务详情 */
    @GetMapping("/api/user/tasks/{id}")
    public Result<TaskInfo> webDetail(@PathVariable Long id) {
        TaskInfo task = taskInfoService.getById(id);
        if (task == null || task.getStatus() != 1) return Result.notFound("任务不存在或已下架");
        return Result.success(task);
    }

    /** 领取任务 */
    @PostMapping("/api/user/tasks/{id}/claim")
    public Result<TaskUserOrder> claim(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id,
                                       HttpServletRequest request) {
        Long userId = getUserId(auth);
        TaskInfo task = taskInfoService.getById(id);
        if (task == null || task.getStatus() != 1) return Result.badRequest("任务不可领取");

        // 同任务不重复
        long cnt = orderService.count(new LambdaQueryWrapper<TaskUserOrder>()
            .eq(TaskUserOrder::getUserId, userId).eq(TaskUserOrder::getTaskId, id));
        if (cnt > 0) return Result.badRequest("已领取过该任务");

        // IP限制
        String ip = getClientIp(request);
        int ipDays = getIpLimitDays();
        LambdaQueryWrapper<TaskUserOrder> ipW = new LambdaQueryWrapper<>();
        // 用 userId 查同IP的历史订单（简化：直接查该用户最近N天是否有订单，实际IP限制需单独IP表，这里用用户最近订单近似）
        if (ipDays > 0) {
            LocalDateTime since = LocalDateTime.now().minusDays(ipDays);
            long recent = orderService.count(new LambdaQueryWrapper<TaskUserOrder>()
                .eq(TaskUserOrder::getUserId, userId).ge(TaskUserOrder::getCreateTime, since));
            if (recent > 0) return Result.badRequest("近" + ipDays + "天内已领取过任务");
        }

        TaskUserOrder order = new TaskUserOrder();
        order.setUserId(userId);
        order.setTaskId(id);
        order.setRewardAmount(task.getRewardAmount());
        order.setGift(task.getGift());
        order.setAuditStatus(1); // 待提交
        order.setExpireTime(LocalDateTime.now().plusMinutes(task.getExpireMinute()));
        orderService.save(order);
        return Result.success("领取成功", order);
    }

    /** 我的订单 */
    @GetMapping("/api/user/tasks/orders")
    public Result<Page<TaskUserOrder>> myOrders(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getUserId(auth);
        return Result.success(orderService.page(new Page<>(page, size),
            new LambdaQueryWrapper<TaskUserOrder>()
                .eq(TaskUserOrder::getUserId, userId)
                .orderByDesc(TaskUserOrder::getCreateTime)));
    }

    /** 提交订单 */
    @PutMapping("/api/user/tasks/orders/{id}/submit")
    public Result<Void> submit(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        Long userId = getUserId(auth);
        TaskUserOrder order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) return Result.notFound("订单不存在");
        if (order.getAuditStatus() != 1) return Result.badRequest("当前状态不可提交");
        if (order.getExpireTime() != null && LocalDateTime.now().isAfter(order.getExpireTime()))
            return Result.badRequest("任务已过期");
        order.setSubmitImages((String) body.getOrDefault("submitImages", ""));
        order.setSubmitNote((String) body.getOrDefault("submitNote", ""));
        order.setAuditStatus(2); // 待审核
        orderService.updateById(order);
        return Result.success();
    }

    /** 领取奖励(提现) */
    @PostMapping("/api/user/tasks/orders/{id}/withdraw")
    public Result<Map<String, Object>> withdraw(@RequestHeader("Authorization") String auth,
                                                @PathVariable Long id) {
        Long userId = getUserId(auth);
        TaskUserOrder order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) return Result.notFound("订单不存在");
        if (order.getGrantPay() != 1) return Result.badRequest("管理员尚未授权打款");
        if (order.getWithdrawStatus() == 2) return Result.badRequest("已提现成功");
        if (order.getWithdrawStatus() == 1) return Result.badRequest("提现处理中");

        String tradeNo = "T" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        TaskPayLog log = new TaskPayLog();
        log.setOrderId(order.getId());
        log.setUserId(userId);
        log.setTradeNo(tradeNo);
        log.setPayAmount(order.getRewardAmount());
        log.setPayStatus(1);
        log.setApplyTime(LocalDateTime.now());
        payLogService.save(log);

        order.setWithdrawStatus(1);
        orderService.updateById(order);

        // 获取 openid（优先从订单，否则查用户绑定）
        String openid = order.getOpenid() != null && !order.getOpenid().isBlank()
            ? order.getOpenid() : getOpenidByUserId(userId);

        // 调用微信支付
        try {
            if (wechatPayService.isConfigured() && openid != null && !openid.isBlank()) {
                JsonNode result = wechatPayService.transferToWallet(
                    openid,
                    order.getRewardAmount().multiply(new java.math.BigDecimal(100)).intValue(), // 分
                    null,
                    "任务奖励"
                );
                wechatPayService.updatePayLog(log, result);
                if (log.getPayStatus() == 2) {
                    order.setWithdrawStatus(2);
                    orderService.updateById(order);
                }
            }
            payLogService.updateById(log);
        } catch (Exception e) {
            log.setFailReason(e.getMessage());
            log.setPayStatus(log.getRetryCount() < 3 ? 1 : 3);
            payLogService.updateById(log);
        }

        return Result.success("提现申请已提交", Map.of("tradeNo", tradeNo, "amount", order.getRewardAmount()));
    }

    /** 我的打款日志 */
    @GetMapping("/api/user/tasks/pay-logs")
    public Result<Page<TaskPayLog>> myPayLogs(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {
        Long userId = getUserId(auth);
        return Result.success(payLogService.page(new Page<>(page, size),
            new LambdaQueryWrapper<TaskPayLog>().eq(TaskPayLog::getUserId, userId).orderByDesc(TaskPayLog::getCreateTime)));
    }

    private int getIpLimitDays() {
        try {
            SysConfig c = sysConfigService.getOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "task_ip_days"));
            return c != null && c.getStatus() == 1 ? Integer.parseInt(c.getConfigValue()) : 7;
        } catch (Exception e) { return 7; }
    }

    private Long getUserId(String auth) { return jwtUtil.getUserIdFromToken(auth.replace("Bearer ", "")); }

    /** 根据 hourLimits 计算 totalNum */
    private void calcTotalFromHours(TaskInfo task) {
        String limits = task.getHourLimits();
        if (limits == null || limits.isBlank()) return;
        try {
            int sum = 0;
            for (String s : limits.split(",")) {
                sum += Integer.parseInt(s.trim());
            }
            task.setTotalNum(sum);
        } catch (NumberFormatException ignored) {}
    }

    private String getClientIp(HttpServletRequest r) {
        String ip = r.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) { ip = r.getHeader("X-Real-IP"); }
        if (ip == null || ip.isBlank()) { ip = r.getRemoteAddr(); }
        return ip;
    }
}
