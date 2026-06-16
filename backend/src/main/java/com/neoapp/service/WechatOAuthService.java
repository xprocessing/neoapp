package com.neoapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoapp.config.WechatOAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatOAuthService {

    private final WechatOAuthProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** 获取微信OAuth授权URL */
    public String buildAuthUrl(String state) {
        String redirect = URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8);
        return "https://open.weixin.qq.com/connect/oauth2/authorize"
            + "?appid=" + props.getAppId()
            + "&redirect_uri=" + redirect
            + "&response_type=code"
            + "&scope=snsapi_userinfo"
            + "&state=" + state + "#wechat_redirect";
    }

    /** code换access_token和openid */
    public WechatUserInfo exchangeCode(String code) throws Exception {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
            + "?appid=" + props.getAppId()
            + "&secret=" + props.getAppSecret()
            + "&code=" + code
            + "&grant_type=authorization_code";

        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode data = mapper.readTree(resp.body());

        if (data.has("errcode") && data.get("errcode").asInt() != 0) {
            throw new RuntimeException("微信登录失败: " + data.get("errmsg").asText());
        }

        WechatUserInfo info = new WechatUserInfo();
        info.openid = data.get("openid").asText();
        info.accessToken = data.get("access_token").asText();
        if (data.has("unionid")) info.unionid = data.get("unionid").asText();

        // 获取用户信息
        String userUrl = "https://api.weixin.qq.com/sns/userinfo"
            + "?access_token=" + info.accessToken
            + "&openid=" + info.openid
            + "&lang=zh_CN";
        HttpRequest userReq = HttpRequest.newBuilder().uri(URI.create(userUrl)).GET().build();
        HttpResponse<String> userResp = httpClient.send(userReq, HttpResponse.BodyHandlers.ofString());
        JsonNode userData = mapper.readTree(userResp.body());
        if (userData.has("nickname")) info.nickname = userData.get("nickname").asText();
        if (userData.has("headimgurl")) info.avatar = userData.get("headimgurl").asText();

        log.info("Wechat login: openid={}, nickname={}", info.openid, info.nickname);
        return info;
    }

    public boolean isConfigured() {
        return props.getAppId() != null && !props.getAppId().isBlank()
            && props.getAppSecret() != null && !props.getAppSecret().isBlank();
    }

    public static class WechatUserInfo {
        public String openid;
        public String unionid;
        public String accessToken;
        public String nickname;
        public String avatar;
    }
}
