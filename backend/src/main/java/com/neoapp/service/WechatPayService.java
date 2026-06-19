package com.neoapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoapp.config.WechatPayProperties;
import com.neoapp.entity.TaskPayLog;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayService {

    private final WechatPayProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            String path = props.getPrivateKeyPath();
            if (path != null && !path.isBlank()) {
                byte[] keyBytes = Files.readAllBytes(Paths.get(path));
                String keyStr = new String(keyBytes, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyStr));
                this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
                log.info("WechatPay private key loaded");
            }
        } catch (Exception e) {
            log.error("Failed to load WechatPay private key: {}", e.getMessage());
        }
    }

    /** 判断是否已配置 */
    public boolean isConfigured() {
        return privateKey != null && props.getMchId() != null && !props.getMchId().isBlank();
    }

    /**
     * 创建 JSAPI/H5 支付订单
     * @return { prepay_id, h5_url }
     */
    public JsonNode createPayOrder(String outTradeNo, String description, int amount, String openid, String clientIp) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("微信支付未配置");

        var body = new java.util.LinkedHashMap<String, Object>();
        String url;
        boolean isPartner = props.getSubMchId() != null && !props.getSubMchId().isBlank();

        if (isPartner) {
            body.put("sp_appid", props.getAppId());
            body.put("sp_mchid", props.getMchId());
            body.put("sub_mchid", props.getSubMchId());
            body.put("description", description);
            body.put("out_trade_no", outTradeNo);
            body.put("notify_url", props.getNotifyUrl() != null ? props.getNotifyUrl() : "");
            var amt = new java.util.LinkedHashMap<String, Object>(); amt.put("total", amount); amt.put("currency", "CNY");
            body.put("amount", amt);
            var scene = new java.util.LinkedHashMap<String, Object>(); scene.put("payer_client_ip", clientIp);
            body.put("scene_info", scene);
            url = "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/h5";
        } else {
            body.put("appid", props.getAppId());
            body.put("mchid", props.getMchId());
            body.put("description", description);
            body.put("out_trade_no", outTradeNo);
            body.put("notify_url", props.getNotifyUrl() != null ? props.getNotifyUrl() : "");
            var amt = new java.util.LinkedHashMap<String, Object>(); amt.put("total", amount); amt.put("currency", "CNY");
            body.put("amount", amt);
            var scene = new java.util.LinkedHashMap<String, Object>(); scene.put("payer_client_ip", clientIp);
            body.put("scene_info", scene);
            url = "https://api.mch.weixin.qq.com/v3/pay/transactions/h5";
        }

        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url)).header("Content-Type", "application/json").header("Accept", "application/json")
            .header("Authorization", buildAuthHeader("POST", url, json))
            .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode result = mapper.readTree(resp.body());
        if (resp.statusCode() == 200) {
            log.info("WechatPay order created: outTradeNo={}, prepay_id={}", outTradeNo, result.get("prepay_id").asText());
            return result;
        } else {
            String errMsg = result.has("message") ? result.get("message").asText() : "unknown";
            throw new RuntimeException("微信下单失败: " + errMsg);
        }
    }

    /**
     * 企业付款到零钱（单笔）
     * @return { out_bill_no, state, transfer_bill_no }
     */
    public JsonNode transferToWallet(String openid, int amount, String userName, String remark) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("微信支付未配置");

        String outBillNo = "TR" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().substring(0, 6);

        // 转账场景报备信息（场景1005佣金报酬）
        var reportInfo1 = new java.util.LinkedHashMap<String, Object>();
        reportInfo1.put("info_type", "劳务关系");
        reportInfo1.put("info_content", "平台任务推广劳务合作");
        var reportInfo2 = new java.util.LinkedHashMap<String, Object>();
        reportInfo2.put("info_type", "报酬说明");
        reportInfo2.put("info_content", "完成平台推广任务获得佣金报酬");

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("appid", props.getAppId());
        body.put("out_bill_no", outBillNo);
        body.put("transfer_scene_id", "1005");
        body.put("openid", openid);
        body.put("transfer_amount", amount);
        body.put("transfer_remark", remark != null ? remark : "消费履约补贴");
        body.put("notify_url", props.getNotifyUrl() != null ? props.getNotifyUrl() : "");
        body.put("transfer_scene_report_infos", java.util.List.of(reportInfo1, reportInfo2));

        String jsonBody = mapper.writeValueAsString(body);

        String url = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", buildAuthHeader("POST", url, jsonBody))
            .header("Wechatpay-Serial", props.getMchSerialNo())
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode result = mapper.readTree(response.body());

        if (response.statusCode() == 200) {
            log.info("WechatPay transfer success: outBillNo={}, amount={}", outBillNo, amount);
            return result;
        } else {
            String errMsg = result.has("message") ? result.get("message").asText() : "unknown";
            log.error("WechatPay transfer failed: code={}, msg={}", response.statusCode(), errMsg);
            throw new RuntimeException("微信打款失败: " + errMsg);
        }
    }

    /** 查询转账结果 */
    public JsonNode queryTransfer(String outBillNo) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("微信支付未配置");

        String url = "https://api.mch.weixin.qq.com/v3/fund-app/mch-transfer/transfer-bills/out-bill-no/" + outBillNo;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .header("Authorization", buildAuthHeader("GET", url, ""))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("WechatPay query result: {}", response.body());
        return mapper.readTree(response.body());
    }

    /** 验证回调签名 */
    public boolean verifySignature(String wechatpayTimestamp, String wechatpayNonce,
                                   String wechatpaySignature, String body) {
        try {
            String message = wechatpayTimestamp + "\n" + wechatpayNonce + "\n" + body + "\n";
            Signature sign = Signature.getInstance("SHA256withRSA");
            PublicKey pubKey = getPublicKey();
            sign.initVerify(pubKey);
            sign.update(message.getBytes(StandardCharsets.UTF_8));
            return sign.verify(Base64.getDecoder().decode(wechatpaySignature));
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    private PublicKey getPublicKey() {
        try {
            if (privateKey instanceof RSAPrivateCrtKey rsaKey) {
                java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(
                    rsaKey.getModulus(), rsaKey.getPublicExponent());
                return KeyFactory.getInstance("RSA").generatePublic(spec);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** 更新打款日志(回调或查询) */
    public void updatePayLog(TaskPayLog log, JsonNode result) {
        if (result == null) return;
        String state = result.has("state") ? result.get("state").asText() : "";
        if ("SUCCESS".equals(state)) {
            log.setPayStatus(2);
            log.setPayTime(java.time.LocalDateTime.now());
            if (result.has("transfer_bill_no")) log.setWechatPayNo(result.get("transfer_bill_no").asText());
        } else if ("FAIL".equals(state) || "CANCELED".equals(state)) {
            log.setPayStatus(3);
            if (result.has("fail_reason")) log.setFailReason(result.get("fail_reason").asText());
        }
    }

    // ----------------- private helpers -----------------

    private String buildAuthHeader(String method, String url, String body) throws Exception {
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        long timestamp = Instant.now().getEpochSecond();
        String signMessage = method + "\n" + URI.create(url).getPath()
            + (url.contains("?") ? "?" + url.substring(url.indexOf("?") + 1) : "")
            + "\n" + timestamp + "\n" + nonce + "\n" + body + "\n";

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(signMessage.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(sign.sign());

        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + props.getMchId()
            + "\",nonce_str=\"" + nonce + "\",signature=\"" + signature
            + "\",timestamp=\"" + timestamp + "\",serial_no=\"" + props.getMchSerialNo() + "\"";
    }
}
