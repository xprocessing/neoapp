package com.neoapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddressRequest {

    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    @NotBlank(message = "收货人电话不能为空")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 是否设为默认地址 */
    private Integer isDefault;
}
