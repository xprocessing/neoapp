package com.neoapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neoapp.common.Result;
import com.neoapp.dto.UserAddressRequest;
import com.neoapp.dto.UserAddressVO;
import com.neoapp.entity.UserAddress;
import com.neoapp.service.UserAddressService;
import com.neoapp.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户的收货地址列表
     */
    @GetMapping
    public Result<List<UserAddressVO>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        List<UserAddress> list = addressService.list(
            new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime)
        );
        return Result.success(list.stream().map(this::toVO).toList());
    }

    /**
     * 新增收货地址
     */
    @PostMapping
    public Result<UserAddressVO> create(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserAddressRequest request) {
        Long userId = getUserId(authHeader);

        // 如果设为默认，取消其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId);
        }

        // 如果是第一个地址，自动设为默认
        long count = addressService.count(
            new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
        );
        int isDefault = (count == 0) ? 1 : (request.getIsDefault() != null ? request.getIsDefault() : 0);

        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setName(request.getName());
        addr.setPhone(request.getPhone());
        addr.setProvince(request.getProvince());
        addr.setCity(request.getCity());
        addr.setDistrict(request.getDistrict());
        addr.setDetail(request.getDetail());
        addr.setIsDefault(isDefault);

        addressService.save(addr);
        return Result.success("添加成功", toVO(addr));
    }

    /**
     * 更新收货地址
     */
    @PutMapping("/{id}")
    public Result<UserAddressVO> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody UserAddressRequest request) {
        Long userId = getUserId(authHeader);
        UserAddress addr = addressService.getById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            return Result.notFound("地址不存在");
        }

        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId);
        }

        addr.setName(request.getName());
        addr.setPhone(request.getPhone());
        addr.setProvince(request.getProvince());
        addr.setCity(request.getCity());
        addr.setDistrict(request.getDistrict());
        addr.setDetail(request.getDetail());
        if (request.getIsDefault() != null) {
            addr.setIsDefault(request.getIsDefault());
        }

        addressService.updateById(addr);
        return Result.success("更新成功", toVO(addr));
    }

    /**
     * 删除收货地址（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = getUserId(authHeader);
        UserAddress addr = addressService.getById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            return Result.notFound("地址不存在");
        }
        addressService.removeById(id);
        return Result.success();
    }

    /**
     * 设为默认地址
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = getUserId(authHeader);
        UserAddress addr = addressService.getById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            return Result.notFound("地址不存在");
        }

        clearDefault(userId);
        addr.setIsDefault(1);
        addressService.updateById(addr);
        return Result.success();
    }

    private void clearDefault(Long userId) {
        List<UserAddress> defaults = addressService.list(
            new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1)
        );
        for (UserAddress a : defaults) {
            a.setIsDefault(0);
            addressService.updateById(a);
        }
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    private UserAddressVO toVO(UserAddress addr) {
        UserAddressVO vo = new UserAddressVO();
        vo.setId(addr.getId());
        vo.setName(addr.getName());
        vo.setPhone(addr.getPhone());
        vo.setProvince(addr.getProvince());
        vo.setCity(addr.getCity());
        vo.setDistrict(addr.getDistrict());
        vo.setDetail(addr.getDetail());
        vo.setIsDefault(addr.getIsDefault());
        vo.setCreateTime(addr.getCreateTime());
        return vo;
    }
}
