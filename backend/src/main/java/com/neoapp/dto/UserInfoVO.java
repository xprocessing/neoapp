package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息 VO，用于前端展示（脱敏）
 */
@Data
public class UserInfoVO {

    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer sex;
    private Integer status;
    private String inviteCode;
    private Long parentId;
    private LocalDateTime createTime;

    /** 默认收货地址（省市区+详细） */
    private String defaultAddress;

    /** 最后登录IP */
    private String lastLoginIp;
}
