package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.InviteUser;
import com.neoapp.mapper.InviteUserMapper;
import com.neoapp.service.InviteUserService;
import org.springframework.stereotype.Service;

@Service
public class InviteUserServiceImpl extends ServiceImpl<InviteUserMapper, InviteUser> implements InviteUserService {
}
