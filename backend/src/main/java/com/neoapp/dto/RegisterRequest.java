package com.neoapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Size(min = 11, max = 11, message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32位")
    private String password;

    @Size(max = 50, message = "昵称最多50个字符")
    private String nickname;

    /** 邀请码（可选） */
    private String inviteCode;
}
