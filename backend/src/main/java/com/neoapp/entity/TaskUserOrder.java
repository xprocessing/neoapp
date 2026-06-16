package com.neoapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_user_order")
public class TaskUserOrder implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String openid;
    private BigDecimal rewardAmount;
    private String gift;
    private Integer auditStatus;
    private Integer grantPay;
    private Integer withdrawStatus;
    private LocalDateTime expireTime;
    private String submitImages;
    private String submitNote;
    private String auditNote;
    private LocalDateTime auditTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
