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
