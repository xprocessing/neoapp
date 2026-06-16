package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.UserWechat;
import com.neoapp.mapper.UserWechatMapper;
import com.neoapp.service.UserWechatService;
import org.springframework.stereotype.Service;

@Service
public class UserWechatServiceImpl extends ServiceImpl<UserWechatMapper, UserWechat> implements UserWechatService {
}
