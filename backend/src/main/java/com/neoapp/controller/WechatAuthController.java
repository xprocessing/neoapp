package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neoapp.common.Result;
import com.neoapp.dto.LoginRequest;
import com.neoapp.dto.RegisterRequest;
import com.neoapp.dto.UserInfoVO;
import com.neoapp.entity.UserInfo;
import com.neoapp.entity.UserWechat;
import com.neoapp.service.*;
import com.neoapp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/wechat")
@RequiredArgsConstructor
public class WechatAuthController {

    private final WechatOAuthService oauthService;
    private final UserWechatService userWechatService;
    private final UserInfoService userInfoService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /** 获取微信授权URL */
    @GetMapping("/url")
    public Result<Map<String, String>> buildUrl(@RequestParam(defaultValue = "login") String state) {
        if (!oauthService.isConfigured()) return Result.badRequest("微信登录未配置");
        return Result.success(Map.of("url", oauthService.buildAuthUrl(state)));
    }

    /** 微信OAuth回调登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (code == null || code.isBlank()) return Result.badRequest("授权码不能为空");

        try {
            WechatOAuthService.WechatUserInfo wx = oauthService.exchangeCode(code);

            // 查找是否已绑定
            UserWechat uw = userWechatService.getOne(
                new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getOpenid, wx.openid));
            if (uw != null) {
                // 已绑定，直接登录
                UserInfo user = userInfoService.getById(uw.getUserId());
                if (user == null || user.getStatus() != 1) return Result.badRequest("用户不存在或已禁用");
                String token = jwtUtil.generateToken(user.getId(), user.getPhone());
                return Result.success(Map.of("token", token, "bound", true));
            }

            // 未绑定，返回openid让前端引导注册
            String state = body.getOrDefault("phone", null);
            String password = body.getOrDefault("password", null);

            if (StringUtils.hasText(state) && StringUtils.hasText(password)) {
                // 前端已提交手机号密码，直接注册+绑定
                RegisterRequest reg = new RegisterRequest();
                reg.setPhone(state);
                reg.setPassword(password);
                reg.setNickname(wx.nickname);
                reg.setInviteCode(body.getOrDefault("inviteCode", null));
                UserInfoVO vo = userService.register(reg);

                saveWechat(vo.getId(), wx);
                String token = jwtUtil.generateToken(vo.getId(), state);
                return Result.success(Map.of("token", token, "bound", true));
            }

            return Result.success(Map.of("openid", wx.openid, "nickname", wx.nickname,
                "avatar", wx.avatar != null ? wx.avatar : "", "bound", false));
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    /** 绑定微信 */
    @PostMapping("/bind")
    public Result<Void> bind(@RequestHeader("Authorization") String auth,
                             @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
        String code = body.get("code");
        if (code == null || code.isBlank()) return Result.badRequest("授权码不能为空");

        try {
            WechatOAuthService.WechatUserInfo wx = oauthService.exchangeCode(code);

            // 检查openid是否已绑定
            if (userWechatService.count(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getOpenid, wx.openid)) > 0) {
                return Result.badRequest("该微信已绑定其他账号");
            }
            if (userWechatService.count(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getUserId, userId)) > 0) {
                return Result.badRequest("当前账号已绑定微信");
            }

            saveWechat(userId, wx);
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    private void saveWechat(Long userId, WechatOAuthService.WechatUserInfo wx) {
        UserWechat uw = new UserWechat();
        uw.setUserId(userId);
        uw.setOpenid(wx.openid);
        uw.setUnionid(wx.unionid);
        uw.setAppType("mp");
        userWechatService.save(uw);
    }
}
