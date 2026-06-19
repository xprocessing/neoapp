package com.neoapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {

    /** 微信公众号/小程序 AppID */
    private String appId;

    /** 微信商户号 */
    private String mchId;

    /** API v3 密钥 */
    private String apiV3Key;

    /** 商户证书序列号 */
    private String mchSerialNo;

    /** 商户私钥路径 (classpath:/cert/apiclient_key.pem) */
    private String privateKeyPath;

    /** 支付回调通知地址 */
    private String notifyUrl;

    /** 子商户号（服务商模式） */
    private String subMchId;
}
