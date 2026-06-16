package com.neoapp.dto;

import lombok.Data;

@Data
public class AdminUserPageQuery {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Integer status;
}
