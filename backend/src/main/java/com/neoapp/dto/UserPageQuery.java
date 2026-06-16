package com.neoapp.dto;

import lombok.Data;

@Data
public class UserPageQuery {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private String phone;
    private Integer status;
}
