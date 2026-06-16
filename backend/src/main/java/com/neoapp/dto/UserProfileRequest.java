package com.neoapp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户自助更新个人资料请求
 */
@Data
public class UserProfileRequest {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    /** 性别：0未知 1男 2女 */
    private Integer sex;

    /** 头像 URL（COS 返回的地址） */
    private String avatar;
}
