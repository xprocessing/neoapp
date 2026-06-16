package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.TaskPayLog;
import com.neoapp.mapper.TaskPayLogMapper;
import com.neoapp.service.TaskPayLogService;
import org.springframework.stereotype.Service;

@Service
public class TaskPayLogServiceImpl extends ServiceImpl<TaskPayLogMapper, TaskPayLog> implements TaskPayLogService {}
