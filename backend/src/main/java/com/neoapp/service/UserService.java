package com.neoapp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.BusinessException;
import com.neoapp.dto.*;
import com.neoapp.entity.*;
import com.neoapp.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserInfoService userInfoService;
    private final UserAddressService userAddressService;
    private final PointUserService pointUserService;
    private final PointLogService pointLogService;
    private final SysConfigService sysConfigService;
    private final InviteUserService inviteUserService;
    private final InviteRewardLogService inviteRewardLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public UserInfoVO register(RegisterRequest request) {
        // 检查手机号是否已注册
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getPhone, request.getPhone());
        if (userInfoService.count(wrapper) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        UserInfo user = new UserInfo();
        user.setPhone(request.getPhone());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : "用户" + request.getPhone().substring(7));
        user.setStatus(1);
        user.setSex(0);
        user.setInviteCode(generateInviteCode());

        // 处理邀请码
        if (StringUtils.hasText(request.getInviteCode())) {
            LambdaQueryWrapper<UserInfo> inviterWrapper = new LambdaQueryWrapper<>();
            inviterWrapper.eq(UserInfo::getInviteCode, request.getInviteCode());
            UserInfo parent = userInfoService.getOne(inviterWrapper);
            if (parent != null) {
                user.setParentId(parent.getId());
            }
        }

        userInfoService.save(user);

        // 注册送积分
        grantRegisterPoints(user.getId());

        // 邀请奖励
        if (user.getParentId() != null && user.getParentId() > 0) {
            grantInviteReward(user.getParentId(), user.getId());
        }

        return toVO(user);
    }

    /**
     * 用户登录（手机号 + 密码）
     */
    public String login(LoginRequest request, String clientIp) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getPhone, request.getPhone());
        UserInfo user = userInfoService.getOne(wrapper);

        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已禁用");
        }

        // 更新最后登录
        user.setLastLoginTime(java.time.LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userInfoService.updateById(user);

        return jwtUtil.generateToken(user.getId(), user.getPhone());
    }

    /**
     * 获取用户信息
     */
    public UserInfoVO getUserInfo(Long userId) {
        UserInfo user = userInfoService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    /**
     * 管理端 - 分页查询用户列表
     */
    public Page<UserInfoVO> getUserPage(UserPageQuery query) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                .like(UserInfo::getNickname, query.getKeyword())
                .or()
                .like(UserInfo::getPhone, query.getKeyword())
            );
        }
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(UserInfo::getPhone, query.getPhone());
        }
        if (query.getStatus() != null) {
            wrapper.eq(UserInfo::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(UserInfo::getCreateTime);

        Page<UserInfo> page = userInfoService.page(
            new Page<>(query.getPage(), query.getSize()), wrapper
        );

        Page<UserInfoVO> voPage = new Page<>();
        voPage.setCurrent(page.getCurrent());
        voPage.setSize(page.getSize());
        voPage.setTotal(page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());

        // 批量填充默认地址
        if (!voPage.getRecords().isEmpty()) {
            List<Long> userIds = voPage.getRecords().stream().map(UserInfoVO::getId).toList();
            Map<Long, String> addrMap = userAddressService.list(
                new LambdaQueryWrapper<UserAddress>()
                    .in(UserAddress::getUserId, userIds)
                    .eq(UserAddress::getIsDefault, 1)
            ).stream().collect(Collectors.toMap(
                UserAddress::getUserId,
                a -> a.getProvince() + a.getCity() + a.getDistrict() + " " + a.getDetail(),
                (a, b) -> a
            ));
            voPage.getRecords().forEach(vo -> vo.setDefaultAddress(addrMap.get(vo.getId())));
        }

        return voPage;
    }

    /**
     * 管理端 - 更新用户信息
     */
    public UserInfoVO updateUser(Long userId, UserUpdateRequest request) {
        UserInfo user = userInfoService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        userInfoService.updateById(user);
        return toVO(user);
    }

    /**
     * 用户自助更新个人资料
     */
    public UserInfoVO updateProfile(Long userId, UserProfileRequest request) {
        UserInfo user = userInfoService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        userInfoService.updateById(user);
        return toVO(user);
    }

    /**
     * 管理端 - 逻辑删除用户
     */
    public void deleteUser(Long userId) {
        UserInfo user = userInfoService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userInfoService.removeById(userId);
    }

    // ---------- 工具方法 ----------

    /**
     * 注册送积分
     */
    private void grantRegisterPoints(Long userId) {
        try {
            SysConfig config = sysConfigService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "point_register"));
            if (config == null || config.getStatus() != 1) return;

            int point = Integer.parseInt(config.getConfigValue());
            if (point <= 0) return;

            // 创建或更新积分账户
            PointUser pu = pointUserService.getOne(
                new LambdaQueryWrapper<PointUser>().eq(PointUser::getUserId, userId));
            if (pu == null) {
                pu = new PointUser();
                pu.setUserId(userId);
                pu.setTotalPoint(point);
                pu.setUsablePoint(point);
                pointUserService.save(pu);
            } else {
                pu.setTotalPoint(pu.getTotalPoint() + point);
                pu.setUsablePoint(pu.getUsablePoint() + point);
                pointUserService.updateById(pu);
            }

            // 积分流水
            PointLog log = new PointLog();
            log.setUserId(userId);
            log.setPoint(point);
            log.setBalance(pu.getUsablePoint());
            log.setType(1);
            log.setSource("register");
            log.setRemark("新用户注册赠送" + point + "积分");
            pointLogService.save(log);
        } catch (Exception e) {
            // 积分赠送失败不影响注册
        }
    }

    /**
     * 邀请奖励
     */
    private void grantInviteReward(Long parentId, Long childId) {
        try {
            SysConfig config = sysConfigService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, "point_invite"));
            if (config == null || config.getStatus() != 1) return;

            int point = Integer.parseInt(config.getConfigValue());
            if (point <= 0) return;

            // 更新邀请关系
            InviteUser iu = inviteUserService.getOne(
                new LambdaQueryWrapper<InviteUser>().eq(InviteUser::getUserId, parentId));
            if (iu == null) {
                iu = new InviteUser();
                iu.setUserId(parentId);
                iu.setInviteNum(1);
                inviteUserService.save(iu);
            } else {
                iu.setInviteNum(iu.getInviteNum() + 1);
                inviteUserService.updateById(iu);
            }

            // 更新被邀请人关系
            InviteUser ci = inviteUserService.getOne(
                new LambdaQueryWrapper<InviteUser>().eq(InviteUser::getUserId, childId));
            if (ci == null) {
                ci = new InviteUser();
                ci.setUserId(childId);
                ci.setParentId(parentId);
                ci.setInviteNum(0);
                inviteUserService.save(ci);
            }

            // 给邀请人加积分
            PointUser pu = pointUserService.getOne(
                new LambdaQueryWrapper<PointUser>().eq(PointUser::getUserId, parentId));
            if (pu == null) {
                pu = new PointUser();
                pu.setUserId(parentId);
                pu.setTotalPoint(point);
                pu.setUsablePoint(point);
                pointUserService.save(pu);
            } else {
                pu.setTotalPoint(pu.getTotalPoint() + point);
                pu.setUsablePoint(pu.getUsablePoint() + point);
                pointUserService.updateById(pu);
            }

            // 积分流水
            PointLog plog = new PointLog();
            plog.setUserId(parentId);
            plog.setPoint(point);
            plog.setBalance(pu.getUsablePoint());
            plog.setType(1);
            plog.setSource("invite");
            plog.setRemark("邀请用户赠送" + point + "积分");
            pointLogService.save(plog);

            // 邀请奖励记录
            InviteRewardLog reward = new InviteRewardLog();
            reward.setUserId(parentId);
            reward.setInviteUserId(childId);
            reward.setRewardType(1);
            reward.setRewardNum(point);
            reward.setRemark("邀请用户赠送" + point + "积分");
            inviteRewardLogService.save(reward);
        } catch (Exception e) {
            // 奖励失败不影响注册
        }
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private UserInfoVO toVO(UserInfo user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setSex(user.getSex());
        vo.setStatus(user.getStatus());
        vo.setInviteCode(user.getInviteCode());
        vo.setParentId(user.getParentId());
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginIp(user.getLastLoginIp());
        return vo;
    }
}
