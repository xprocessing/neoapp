package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoapp.entity.TaskPayLog;
import com.neoapp.entity.TaskUserOrder;
import com.neoapp.service.TaskPayLogService;
import com.neoapp.service.TaskUserOrderService;
import com.neoapp.service.WechatPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/public/wechat-pay")
@RequiredArgsConstructor
public class WechatPayController {

    private final WechatPayService wechatPayService;
    private final TaskPayLogService payLogService;
    private final TaskUserOrderService orderService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 微信支付回调通知
     */
    @PostMapping("/callback")
    public ResponseEntity<String> callback(HttpServletRequest request, @RequestBody String body) {
        try {
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");

            log.info("WechatPay callback: timestamp={}, nonce={}", timestamp, nonce);
            log.debug("WechatPay callback body: {}", body);

            // 验证签名
            if (!wechatPayService.verifySignature(timestamp, nonce, signature, body)) {
                log.warn("WechatPay signature verification failed");
                return ResponseEntity.status(401).body("{\"code\":\"FAIL\",\"message\":\"签名验证失败\"}");
            }

            // 解析回调数据
            JsonNode root = mapper.readTree(body);
            String outBillNo = root.get("out_bill_no").asText();
            String state = root.get("state").asText();
            String transferBillNo = root.has("transfer_bill_no") ? root.get("transfer_bill_no").asText() : "";

            // 查找本地打款日志
            TaskPayLog payLog = payLogService.getOne(
                new LambdaQueryWrapper<TaskPayLog>().eq(TaskPayLog::getTradeNo, outBillNo));
            if (payLog == null) {
                log.warn("Pay log not found for outBillNo: {}", outBillNo);
                return ResponseEntity.ok("{\"code\":\"SUCCESS\"}");
            }

            // 更新打款状态
            if ("SUCCESS".equals(state)) {
                payLog.setPayStatus(2);
                payLog.setPayTime(LocalDateTime.now());
                payLog.setWechatPayNo(transferBillNo);

                // 更新订单提现状态
                TaskUserOrder order = orderService.getById(payLog.getOrderId());
                if (order != null) {
                    order.setWithdrawStatus(2);
                    orderService.updateById(order);
                }
            } else if ("FAIL".equals(state)) {
                payLog.setPayStatus(3);
                payLog.setFailReason(root.has("fail_reason") ? root.get("fail_reason").asText() : "微信打款失败");
            }
            payLogService.updateById(payLog);

            log.info("WechatPay callback processed: outBillNo={}, state={}", outBillNo, state);
            return ResponseEntity.ok("{\"code\":\"SUCCESS\"}");
        } catch (Exception e) {
            log.error("WechatPay callback error", e);
            return ResponseEntity.status(500).body("{\"code\":\"FAIL\",\"message\":\"处理失败\"}");
        }
    }
}
