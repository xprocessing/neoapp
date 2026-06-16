package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 后台管理员 VO（不含密码）
 */
@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
}
