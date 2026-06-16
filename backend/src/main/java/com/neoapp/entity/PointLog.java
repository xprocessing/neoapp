package com.neoapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("point_log")
public class PointLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 变动积分 */
    private Integer point;

    /** 变动后可用积分 */
    private Integer balance;

    /** 类型：1获取 2消耗 */
    private Integer type;

    /** 来源 */
    private String source;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
