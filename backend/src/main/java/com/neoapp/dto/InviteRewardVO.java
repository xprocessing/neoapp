package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InviteRewardVO {

    private Long id;
    private Long userId;
    private String userNickname;
    private Long inviteUserId;
    private String inviteUserNickname;
    private Integer rewardType;
    private Integer rewardNum;
    private String remark;
    private LocalDateTime createTime;
}
