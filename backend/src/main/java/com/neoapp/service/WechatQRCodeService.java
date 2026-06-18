package com.neoapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信扫码登录/绑定 状态管理
 * 用于 PC 端二维码扫码绑定流程
 */
@Slf4j
@Service
public class WechatQRCodeService {

    /**
     * state -> 扫码状态
     */
    private final ConcurrentHashMap<String, QRState> states = new ConcurrentHashMap<>();

    /**
     * 生成二维码 state
     * @param action 操作类型: login / bind
     */
    public String generate(String action) {
        String state = UUID.randomUUID().toString().replace("-", "");
        QRState qs = new QRState();
        qs.state = state;
        qs.action = action;
        qs.createdAt = Instant.now();
        qs.status = "pending";
        states.put(state, qs);
        return state;
    }

    /**
     * 查询扫码状态
     */
    public QRState getStatus(String state) {
        QRState qs = states.get(state);
        if (qs == null) return null;
        // 5分钟超时
        if (qs.status.equals("pending") && Instant.now().isAfter(qs.createdAt.plusSeconds(300))) {
            qs.status = "expired";
        }
        return qs;
    }

    /**
     * 微信回调：保存用户信息
     */
    public void onCallback(String state, String code, String openid, String unionid, String nickname, String avatar) {
        QRState qs = states.get(state);
        if (qs == null) return;
        qs.status = "scanned";
        qs.code = code;
        qs.openid = openid;
        qs.unionid = unionid;
        qs.nickname = nickname;
        qs.avatar = avatar;
    }

    /**
     * 定时清理过期 state（每分钟）
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpired() {
        Instant cutoff = Instant.now().minusSeconds(600);
        states.entrySet().removeIf(e -> e.getValue().createdAt.isBefore(cutoff));
    }

    public static class QRState {
        public String state;
        public String action;
        public String status;     // pending / scanned / expired
        public Instant createdAt;
        public String code;
        public String openid;
        public String unionid;
        public String nickname;
        public String avatar;
    }
}
