package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InviteUserVO {

    private Long userId;
    private String nickname;
    private String phone;
    private Long parentId;
    private String parentNickname;
    private Integer inviteNum;
    private LocalDateTime createTime;
}
