package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neoapp.common.Result;
import com.neoapp.entity.*;
import com.neoapp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserInfoService userInfoService;
    private final TaskInfoService taskInfoService;
    private final TaskUserOrderService orderService;
    private final TaskPayLogService payLogService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Map<String, Object> data = new LinkedHashMap<>();

        // 用户总量
        data.put("totalUsers", userInfoService.count());
        // 今日新增
        data.put("todayUsers", userInfoService.count(
            new LambdaQueryWrapper<UserInfo>().ge(UserInfo::getCreateTime, todayStart)
                .le(UserInfo::getCreateTime, todayEnd)));

        // 上架任务数
        data.put("activeTasks", taskInfoService.count(
            new LambdaQueryWrapper<TaskInfo>().eq(TaskInfo::getStatus, 1)));

        // 今日领取订单数
        data.put("todayOrders", orderService.count(
            new LambdaQueryWrapper<TaskUserOrder>().ge(TaskUserOrder::getCreateTime, todayStart)
                .le(TaskUserOrder::getCreateTime, todayEnd)));

        // 已领订单总数
        data.put("totalOrders", orderService.count());

        // 今日打款金额
        BigDecimal todayPay = payLogService.list(
            new LambdaQueryWrapper<TaskPayLog>().eq(TaskPayLog::getPayStatus, 2)
                .ge(TaskPayLog::getPayTime, todayStart).le(TaskPayLog::getPayTime, todayEnd))
            .stream().map(TaskPayLog::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todayPayAmount", todayPay);

        // 总打款金额
        BigDecimal totalPay = payLogService.list(
            new LambdaQueryWrapper<TaskPayLog>().eq(TaskPayLog::getPayStatus, 2))
            .stream().map(TaskPayLog::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalPayAmount", totalPay);

        return Result.success(data);
    }
}
