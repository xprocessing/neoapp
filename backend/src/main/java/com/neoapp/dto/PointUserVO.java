package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointUserVO {

    private Long id;
    private Long userId;
    private String nickname;
    private String phone;
    private Integer totalPoint;
    private Integer usablePoint;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
