package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.PointUser;
import com.neoapp.mapper.PointUserMapper;
import com.neoapp.service.PointUserService;
import org.springframework.stereotype.Service;

@Service
public class PointUserServiceImpl extends ServiceImpl<PointUserMapper, PointUser> implements PointUserService {
}
