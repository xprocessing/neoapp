package com.neoapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_package")
public class MemberPackage implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String packageName;
    private BigDecimal price;
    private Integer dayNum;
    private Integer givePoint;
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
