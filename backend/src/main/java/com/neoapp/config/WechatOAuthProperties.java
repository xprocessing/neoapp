package com.neoapp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.oauth")
public class WechatOAuthProperties {

    /** 公众号/小程序 AppID */
    private String appId;

    /** 公众号 AppSecret */
    private String appSecret;

    /** OAuth回调地址 */
    private String redirectUri;
}
