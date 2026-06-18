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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/wechat")
@RequiredArgsConstructor
public class WechatAuthController {

    private final WechatOAuthService oauthService;
    private final WechatQRCodeService qrCodeService;
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

    /** PC端扫码绑定 - 生成二维码 */
    @PostMapping("/qrcode/generate")
    public Result<Map<String, String>> generateQRCode() {
        if (!oauthService.isConfigured()) return Result.badRequest("微信登录未配置");
        String state = qrCodeService.generate("bind");
        String qrUrl = oauthService.buildAuthUrl(state);
        return Result.success(Map.of("state", state, "qrUrl", qrUrl));
    }

    /** PC端轮询扫码状态 */
    @GetMapping("/qrcode/status")
    public Result<Map<String, Object>> qrCodeStatus(@RequestParam String state) {
        WechatQRCodeService.QRState qs = qrCodeService.getStatus(state);
        if (qs == null) return Result.badRequest("无效的二维码");
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("status", qs.status);
        if ("scanned".equals(qs.status)) {
            result.put("state", qs.state);
            result.put("nickname", qs.nickname);
            result.put("avatar", qs.avatar);
        }
        return Result.success(result);
    }

    /** PC端扫码 - 使用state完成绑定 */
    @PostMapping("/qrcode/bind")
    public Result<Void> qrCodeBind(@RequestHeader("Authorization") String auth,
                                   @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
        String state = body.get("state");
        WechatQRCodeService.QRState qs = qrCodeService.getStatus(state);
        if (qs == null || !"scanned".equals(qs.status)) return Result.badRequest("未扫码或已过期");

        // 检查openid是否已绑定
        if (userWechatService.count(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getOpenid, qs.openid)) > 0) {
            return Result.badRequest("该微信已绑定其他账号");
        }
        if (userWechatService.count(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getUserId, userId)) > 0) {
            return Result.badRequest("当前账号已绑定微信");
        }

        UserWechat uw = new UserWechat();
        uw.setUserId(userId);
        uw.setOpenid(qs.openid);
        uw.setUnionid(qs.unionid);
        uw.setAppType("mp");
        userWechatService.save(uw);
        return Result.success();
    }

    /** PC端扫码登录 */
    @PostMapping("/qrcode/login")
    public Result<Map<String, Object>> qrCodeLogin(@RequestBody Map<String, String> body) {
        String state = body.get("state");
        WechatQRCodeService.QRState qs = qrCodeService.getStatus(state);
        if (qs == null || !"scanned".equals(qs.status)) return Result.badRequest("未扫码或已过期");

        // 查找是否已绑定
        UserWechat uw = userWechatService.getOne(
            new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getOpenid, qs.openid));
        if (uw != null) {
            UserInfo user = userInfoService.getById(uw.getUserId());
            if (user == null || user.getStatus() != 1) return Result.badRequest("用户不存在或已禁用");
            String token = jwtUtil.generateToken(user.getId(), user.getPhone());
            return Result.success(Map.of("token", token, "bound", true));
        }

        // 未绑定，返回信息供前端注册
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("bound", false);
        result.put("openid", qs.openid);
        result.put("nickname", qs.nickname != null ? qs.nickname : "");
        return Result.success(result);
    }

    /** PC端扫码注册 */
    @PostMapping("/qrcode/register")
    public Result<Map<String, Object>> qrCodeRegister(@RequestBody Map<String, String> body) {
        String state = body.get("state");
        String phone = body.get("phone");
        String password = body.get("password");
        if (state == null || phone == null || password == null) return Result.badRequest("参数不完整");

        WechatQRCodeService.QRState qs = qrCodeService.getStatus(state);
        if (qs == null || !"scanned".equals(qs.status)) return Result.badRequest("未扫码或已过期");

        // 检查手机号是否已注册
        if (userInfoService.count(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, phone)) > 0) {
            return Result.badRequest("该手机号已注册");
        }

        RegisterRequest reg = new RegisterRequest();
        reg.setPhone(phone);
        reg.setPassword(password);
        reg.setNickname(body.getOrDefault("nickname", qs.nickname));
        reg.setInviteCode(body.get("inviteCode"));
        UserInfoVO vo = userService.register(reg);

        UserWechat uw = new UserWechat();
        uw.setUserId(vo.getId());
        uw.setOpenid(qs.openid);
        uw.setUnionid(qs.unionid);
        uw.setAppType("mp");
        userWechatService.save(uw);

        String token = jwtUtil.generateToken(vo.getId(), phone);
        return Result.success(Map.of("token", token, "bound", true));
    }

    /** 获取当前用户微信绑定状态 */
    @GetMapping("/bound")
    public Result<Map<String, Object>> getBoundStatus(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = null;
        try { if (auth != null) userId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", "")); } catch (Exception ignored) {}
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (userId == null) {
            result.put("bound", false);
        } else {
            UserWechat uw = userWechatService.getOne(new LambdaQueryWrapper<UserWechat>().eq(UserWechat::getUserId, userId));
            result.put("bound", uw != null);
            if (uw != null) {
                result.put("openid", uw.getOpenid());
                result.put("bindTime", uw.getCreateTime());
            }
        }
        return Result.success(result);
    }

    /** 微信OAuth回调（公众号服务器配置的回调地址） */
    @GetMapping("/callback")
    public void oauthCallback(@RequestParam String code,
                              @RequestParam String state,
                              jakarta.servlet.http.HttpServletResponse response) throws Exception {
        try {
            WechatOAuthService.WechatUserInfo wx = oauthService.exchangeCode(code);

            // QR码扫码流程（state是UUID格式）→ 存入状态供PC端轮询
            if (state.matches("[a-f0-9]{32}")) {
                qrCodeService.onCallback(state, code, wx.openid, wx.unionid, wx.nickname, wx.avatar);
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<!DOCTYPE html><html><head><meta charset='utf-8'><meta name='viewport' content='width=device-width,initial-scale=1'></head><body style='text-align:center;padding-top:60px;font-family:sans-serif'><h2 style='color:#07c160'>✓ 扫码成功</h2><p style='color:#666'>请返回电脑继续操作</p></body></html>");
                return;
            }

            // 移动端流程 → 重定向到前端页面
            String target = "login".equals(state) ? "/login" : "/profile";
            response.sendRedirect("https://niurouzhou.com" + target
                + "?code=" + code + "&state=" + state);
        } catch (Exception e) {
            log.error("微信回调处理失败", e);
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write("<!DOCTYPE html><html><body style='text-align:center;padding-top:60px'><h2>授权失败</h2><p>" + e.getMessage() + "</p></body></html>");
        }
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
