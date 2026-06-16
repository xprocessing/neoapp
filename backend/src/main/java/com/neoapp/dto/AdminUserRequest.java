package com.neoapp.dto;

import lombok.Data;

/**
 * 管理员 创建/更新 请求
 */
@Data
public class AdminUserRequest {

    private String username;

    /** 密码，创建时必填，更新时选填（不传则不修改） */
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer gender;

    private Integer status;
}
