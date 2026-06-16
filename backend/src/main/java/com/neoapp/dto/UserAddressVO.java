package com.neoapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAddressVO {

    private Long id;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private LocalDateTime createTime;
}
