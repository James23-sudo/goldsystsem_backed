package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gold.goldsystem.dto.TraderDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.entity.TraderEntity;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.TraderMapper;
import com.gold.goldsystem.mapper.UserMapper;
import com.gold.goldsystem.service.TraderService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TraderServiceImpl implements TraderService {
    
    @Resource
    private TraderMapper traderMapper;
    
    @Resource
    private UserMapper userMapper;

    @Override
    public Result addTrader(TraderDTO traderDTO) {
        // 必填字段校验（中文注释）：
        // 订单号、客户账号（6位）、开仓时间、买卖方向、成交量、出入金、隔夜费比例
        if (traderDTO == null) {
            return Result.error(400, "请求体不能为空");
        }
        if (traderDTO.getOrderId() == null || traderDTO.getOrderId().isBlank()) {
            return Result.error(400, "订单号为必填");
        }
        if (traderDTO.getId() == null || traderDTO.getId().length() != 6) {
            return Result.error(400, "用户账号必须是6位字符");
        }
        if (traderDTO.getOpeningTime() == null) {
            return Result.error(400, "开仓时间为必填(yyyy-MM-dd HH:mm:ss)");
        }
        if (traderDTO.getDirection() == null || traderDTO.getDirection().isBlank()) {
            return Result.error(400, "买卖方向为必填");
        }
        if (traderDTO.getVolume() == null) {
            return Result.error(400, "成交量为必填(盎司单位)");
        }
        if (traderDTO.getEntryExit() == null) {
            return Result.error(400, "客户出入金为必填");
        }
        if (traderDTO.getOvernightProportion() == null) {
            return Result.error(400, "隔夜费比例为必填");
        }
        // Check if order ID already exists
        TraderEntity existingTrader = traderMapper.selectById(traderDTO.getOrderId());
        if (existingTrader != null) {
            return Result.error(400, "订单号已存在!");
        }
        
        // Check if user exists
        UserEntity user = userMapper.selectById(traderDTO.getId());
        if (user == null) {
            return Result.error(400, "用户不存在!");
        }
        
        // Convert DTO to Entity
        TraderEntity traderEntity = new TraderEntity()
                .setId(traderDTO.getId())
                .setOrderId(traderDTO.getOrderId())
                .setOpeningTime(traderDTO.getOpeningTime())
                .setClosingTime(traderDTO.getClosingTime())
                .setDirection(traderDTO.getDirection())
                .setVolume(traderDTO.getVolume())
                .setVarieties(traderDTO.getVarieties())
                .setOpeningPrice(traderDTO.getOpeningPrice())
                .setClosingPrice(traderDTO.getClosingPrice())
                .setOvernightPrice(traderDTO.getOvernightPrice())
                .setInoutPrice(traderDTO.getInoutPrice())
                .setIsOk(traderDTO.getIsOk())
                .setStatus(traderDTO.getStatus())
                .setOverPrice(traderDTO.getOverPrice())
                .setEntryExit(traderDTO.getEntryExit())
                .setOvernightProportion(traderDTO.getOvernightProportion());
        
        // Insert into database
        int rows = traderMapper.insert(traderEntity);
        
        if (rows > 0) {
            log.info("Trader added successfully: {}", traderDTO.getOrderId());
            return Result.success(200, "交易订单添加成功");
        } else {
            log.error("Failed to add trader: {}", traderDTO.getOrderId());
            return Result.error(500, "交易订单添加失败");
        }
    }

    // 统一查询使用 queryTraders(isOk, userId)

    /**
     * 通用查询：status=1；isOk=0/1 由前端传参控制
     */
    @Override
    public Result queryTraders(String isOk, String userId) {
        if (isOk == null || isOk.isBlank()) {
            return Result.error(400, "参数isOk为必填，取值0或1");
        }
        if (!"0".equals(isOk) && !"1".equals(isOk)) {
            return Result.error(400, "参数isOk只能为0或1");
        }
        LambdaQueryWrapper<TraderEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(TraderEntity::getStatus, "1");
        qw.eq(TraderEntity::getIsOk, isOk);
        if (userId != null && !userId.isBlank()) {
            qw.eq(TraderEntity::getId, userId);
        }
        qw.select(
                TraderEntity::getId,
                TraderEntity::getOrderId,
                TraderEntity::getOpeningTime,
                TraderEntity::getDirection,
                TraderEntity::getVolume,
                TraderEntity::getVarieties,
                TraderEntity::getOpeningPrice,
                TraderEntity::getClosingTime,
                TraderEntity::getClosingPrice,
                TraderEntity::getOvernightPrice,
                TraderEntity::getInoutPrice
        );
        qw.orderByDesc(TraderEntity::getOpeningTime);
        return Result.success(traderMapper.selectList(qw));
    }

    @Override
    public Result getTradersByUserId(String userId) {
        LambdaQueryWrapper<TraderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TraderEntity::getId, userId);
        List<TraderEntity> traders = traderMapper.selectList(queryWrapper);
        return Result.success(traders);
    }

    @Override
    public Result updateTrader(TraderDTO traderDTO) {
        // Check if trader exists
        TraderEntity existingTrader = traderMapper.selectById(traderDTO.getOrderId());
        if (existingTrader == null) {
            return Result.error(404, "交易订单不存在");
        }
        
        // Convert DTO to Entity
        TraderEntity traderEntity = new TraderEntity()
                .setId(traderDTO.getId())
                .setOrderId(traderDTO.getOrderId())
                .setOpeningTime(traderDTO.getOpeningTime())
                .setClosingTime(traderDTO.getClosingTime())
                .setDirection(traderDTO.getDirection())
                .setVolume(traderDTO.getVolume())
                .setVarieties(traderDTO.getVarieties())
                .setOpeningPrice(traderDTO.getOpeningPrice())
                .setClosingPrice(traderDTO.getClosingPrice())
                .setOvernightPrice(traderDTO.getOvernightPrice())
                .setInoutPrice(traderDTO.getInoutPrice())
                .setIsOk(traderDTO.getIsOk())
                .setStatus(traderDTO.getStatus())
                .setOverPrice(traderDTO.getOverPrice())
                .setEntryExit(traderDTO.getEntryExit())
                .setOvernightProportion(traderDTO.getOvernightProportion());
        
        // Update in database
        int rows = traderMapper.updateById(traderEntity);
        
        if (rows > 0) {
            log.info("Trader updated successfully: {}", traderDTO.getOrderId());
            return Result.success(200, "交易订单更新成功");
        } else {
            log.error("Failed to update trader: {}", traderDTO.getOrderId());
            return Result.error(500, "交易订单更新失败");
        }
    }

    @Override
    public Result deleteTrader(String orderId) {
        // Check if trader exists
        TraderEntity existingTrader = traderMapper.selectById(orderId);
        if (existingTrader == null) {
            return Result.error(404, "交易订单不存在");
        }
        
        // Logical delete: set status to 0
        existingTrader.setStatus("0");
        int rows = traderMapper.updateById(existingTrader);
        
        if (rows > 0) {
            log.info("Trader deleted successfully: {}", orderId);
            return Result.success(200, "交易订单删除成功");
        } else {
            log.error("Failed to delete trader: {}", orderId);
            return Result.error(500, "交易订单删除失败");
        }
    }
}
