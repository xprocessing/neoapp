package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.MemberUser;
import com.neoapp.mapper.MemberUserMapper;
import com.neoapp.service.MemberUserService;
import org.springframework.stereotype.Service;

@Service
public class MemberUserServiceImpl extends ServiceImpl<MemberUserMapper, MemberUser> implements MemberUserService {}
