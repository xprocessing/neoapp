package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.TaskUserOrder;
import com.neoapp.mapper.TaskUserOrderMapper;
import com.neoapp.service.TaskUserOrderService;
import org.springframework.stereotype.Service;

@Service
public class TaskUserOrderServiceImpl extends ServiceImpl<TaskUserOrderMapper, TaskUserOrder> implements TaskUserOrderService {}
