package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.TaskInfo;
import com.neoapp.mapper.TaskInfoMapper;
import com.neoapp.service.TaskInfoService;
import org.springframework.stereotype.Service;

@Service
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskInfoService {}
