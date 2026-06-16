package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neoapp.common.Result;
import com.neoapp.entity.SysConfig;
import com.neoapp.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置管理
 */
@RestController
@RequestMapping("/api/admin/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService configService;

    /**
     * 分页查询
     */
    @GetMapping
    public Result<Page<SysConfig>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysConfig::getConfigKey, keyword)
                .or()
                .like(SysConfig::getConfigName, keyword)
            );
        }
        wrapper.orderByAsc(SysConfig::getConfigKey);
        return Result.success(configService.page(new Page<>(page, size), wrapper));
    }

    /**
     * 获取单个配置
     */
    @GetMapping("/{id}")
    public Result<SysConfig> detail(@PathVariable Long id) {
        SysConfig config = configService.getById(id);
        if (config == null) return Result.notFound("配置不存在");
        return Result.success(config);
    }

    /**
     * 新增配置
     */
    @PostMapping
    public Result<SysConfig> create(@RequestBody SysConfig config) {
        if (!StringUtils.hasText(config.getConfigKey())) {
            return Result.badRequest("配置键名不能为空");
        }
        // 检查唯一
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        if (configService.count(wrapper) > 0) {
            return Result.badRequest("配置键名已存在");
        }
        configService.save(config);
        return Result.success("创建成功", config);
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public Result<SysConfig> update(@PathVariable Long id, @RequestBody SysConfig config) {
        SysConfig exist = configService.getById(id);
        if (exist == null) return Result.notFound("配置不存在");

        if (StringUtils.hasText(config.getConfigKey())) {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
            wrapper.ne(SysConfig::getId, id);
            if (configService.count(wrapper) > 0) {
                return Result.badRequest("配置键名已存在");
            }
            exist.setConfigKey(config.getConfigKey());
        }
        if (config.getConfigName() != null) exist.setConfigName(config.getConfigName());
        if (config.getConfigValue() != null) exist.setConfigValue(config.getConfigValue());
        if (config.getRemark() != null) exist.setRemark(config.getRemark());
        if (config.getStatus() != null) exist.setStatus(config.getStatus());

        configService.updateById(exist);
        return Result.success("更新成功", exist);
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (configService.getById(id) == null) return Result.notFound("配置不存在");
        configService.removeById(id);
        return Result.success();
    }
}
