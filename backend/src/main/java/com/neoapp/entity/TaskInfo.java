package com.neoapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_info")
public class TaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskTitle;
    private String taskCover;
    private String taskIntro;
    private String keyword;
    private String taskContent;
    private BigDecimal rewardAmount;
    private String gift;
    private Integer totalNum;

    /** 24小时每小时任务数量，逗号分隔 */
    private String hourLimits;

    private Integer expireMinute;
    private Integer sort;
    private Integer status;
    @TableLogic(value = "0", delval = "1")
    @TableField("deleted")
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
