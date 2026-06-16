package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.PointLog;
import com.neoapp.mapper.PointLogMapper;
import com.neoapp.service.PointLogService;
import org.springframework.stereotype.Service;

@Service
public class PointLogServiceImpl extends ServiceImpl<PointLogMapper, PointLog> implements PointLogService {
}
