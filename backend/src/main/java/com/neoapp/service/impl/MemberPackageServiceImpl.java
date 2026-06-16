package com.neoapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neoapp.entity.MemberPackage;
import com.neoapp.mapper.MemberPackageMapper;
import com.neoapp.service.MemberPackageService;
import org.springframework.stereotype.Service;

@Service
public class MemberPackageServiceImpl extends ServiceImpl<MemberPackageMapper, MemberPackage> implements MemberPackageService {}
