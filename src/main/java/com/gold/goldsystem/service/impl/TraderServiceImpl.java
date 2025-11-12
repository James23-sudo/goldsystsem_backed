package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gold.goldsystem.dto.TraderDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.entity.TraderEntity;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.TraderMapper;
import com.gold.goldsystem.mapper.UserMapper;
import com.gold.goldsystem.service.TraderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Map;

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

        if (!traderDTO.getDirection().equals("balance")){
            if (traderDTO.getVolume() == null) {
                return Result.error(400, "成交量为必填(盎司单位)");
            }
            if (traderDTO.getOvernightProportion() == null) {
                return Result.error(400, "隔夜费比例为必填");
            }
            if(traderDTO.getTraderSelect() == null){
                return Result.error(400, "交易时间段为必填");
            }
            if (traderDTO.getScheduledTime() == null) {
                return Result.error(400, "预定时间为必填");
            }
        }else{
            if (traderDTO.getEntryExit() == null){
                return Result.error(400, "出入金为必填");
            }
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

        if (traderDTO.getDirection().equals("balance")){
            // 当方向为"balance"时，更新用户余额
            double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
            double entryExitAmount = traderDTO.getEntryExit().doubleValue();
            double newBalance = currentBalance + entryExitAmount;
            
            // 检查新余额是否为负值
            if (newBalance < 0) {
                log.error("用户余额不足: 用户ID={}, 当前余额={}, 请求金额={}", traderDTO.getId(), currentBalance, entryExitAmount);
                return Result.error(400, "余额不足，操作后余额不能为负值");
            }
            
            user.setLeftMoney(String.valueOf(newBalance));
            // 计算可用预付款 = 用户余额 - 已用预付款
            double wasPay = Double.parseDouble(user.getWasPay() != null ? user.getWasPay() : "0");
            double canPay = newBalance - wasPay;
            user.setCanPay(String.valueOf(canPay));
            
            int userUpdateRows = userMapper.updateById(user);
            if (userUpdateRows <= 0) {
                log.error("更新用户余额失败: 用户ID={}", traderDTO.getId());
                return Result.error(500, "更新用户余额失败");
            }
            
            TraderEntity traderEntity = new TraderEntity()
                    .setId(traderDTO.getId())
                    .setOrderId(traderDTO.getOrderId())
                    .setOpeningTime(traderDTO.getOpeningTime())
                    .setDirection(traderDTO.getDirection())
                    .setEntryExit(traderDTO.getEntryExit())
                    .setStatus("1")
                    .setIsOk("1");
            int rows = traderMapper.insert(traderEntity);
            if (rows > 0) {
                log.info("交易订单添加成功并更新用户余额: 订单号={}", traderDTO.getOrderId());
                return Result.success(200, "交易订单添加成功，用户余额已更新");
            } else {
                log.error("添加交易订单失败: 订单号={}", traderDTO.getOrderId());
                return Result.error(500, "交易订单添加失败");
            }
        }

        // 计算并更新用户保证金：成交量 × 100 HKD
        double depositAmount = traderDTO.getVolume().doubleValue() * 100;
        
        // 检查用户可用预付款是否足够扣除保证金
        double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
        double currentWasPay = Double.parseDouble(user.getWasPay() != null ? user.getWasPay() : "0");
        double currentCanPay = currentBalance - currentWasPay;
        
        if (currentCanPay < depositAmount) {
            log.error("用户可用预付款不足: 用户ID={}, 可用预付款={}, 需要保证金={}", traderDTO.getId(), currentCanPay, depositAmount);
            return Result.error(400, "可用预付款不足，无法添加交易订单");
        }
        
        double currentDeposit = Double.parseDouble(user.getDeposit() != null ? user.getDeposit() : "0");
        double newDeposit = currentDeposit + depositAmount;
        user.setDeposit(String.valueOf(newDeposit));
        // 已用预付款与保证金保持一致
        user.setWasPay(String.valueOf(newDeposit));
        // 计算可用预付款 = 用户余额 - 已用预付款
        double canPay = currentBalance - newDeposit;
        user.setCanPay(String.valueOf(canPay));
        
        int userDepositUpdateRows = userMapper.updateById(user);
        if (userDepositUpdateRows <= 0) {
            log.error("Failed to update user deposit for user: {}", traderDTO.getId());
            return Result.error(500, "更新用户保证金失败");
        }
        // Convert DTO to Entity（先赋原始字段）
        TraderEntity traderEntity = new TraderEntity()
                .setId(traderDTO.getId())
                .setOrderId(traderDTO.getOrderId())
                .setOpeningTime(traderDTO.getOpeningTime())
                .setClosingTime(traderDTO.getClosingTime())
                .setScheduledTime(traderDTO.getScheduledTime())
                .setDirection(traderDTO.getDirection())
                .setVolume(traderDTO.getVolume())
                .setVarieties(traderDTO.getVarieties())
                .setOpeningPrice(traderDTO.getOpeningPrice())
                .setClosingPrice(traderDTO.getClosingPrice())
                .setEntryExit(traderDTO.getEntryExit())
                .setOvernightProportion(traderDTO.getOvernightProportion())
                .setTraderSelect(traderDTO.getTraderSelect())
                .setTraderCloseSelect(traderDTO.getTraderCloseSelect())
                .setDeposit(new BigDecimal(depositAmount))
                .setStatus(traderDTO.getStatus())
                .setIsOk(traderDTO.getIsOk());

        // 自动计算派生字段
        try {
            BigDecimal openingPrice = traderEntity.getOpeningPrice();
            BigDecimal closingPrice = traderEntity.getClosingPrice();
            BigDecimal volume = traderEntity.getVolume(); // 盎司

            // 盈亏：依据方向 buy/sell
            if (openingPrice != null && closingPrice != null && volume != null) {
                String direction = traderEntity.getDirection();
                BigDecimal pnl = null;
                if (direction != null) {
                    if ("sell".equalsIgnoreCase(direction)) {
                        pnl = openingPrice.subtract(closingPrice).multiply(volume);
                    } else if ("buy".equalsIgnoreCase(direction)) {
                        pnl = closingPrice.subtract(openingPrice).multiply(volume);
                    }
                }
                if (pnl != null) {
                    traderEntity.setInoutPrice(pnl.setScale(2, RoundingMode.HALF_UP));
                }
            }
        } catch (Exception e) {
            log.warn("新增订单派生字段计算异常: {}", e.getMessage());
        }

        // 可选字段齐全（开仓价、平仓价、收盘价、平仓时间）则自动置 isOk=1
        boolean optionalComplete = traderEntity.getOpeningPrice() != null
                && traderEntity.getClosingPrice() != null
                && traderEntity.getScheduledTime() != null
                && traderEntity.getClosingTime() != null;
        if (optionalComplete) {
            traderEntity.setIsOk("1");
        }
        
        // 如果isOk=1，需要处理保证金返还和盈亏结算
        if ("1".equals(traderEntity.getIsOk())) {
            // 返还保证金：从已扣除的保证金中减去
            double finalDeposit = currentDeposit; // 不增加保证金，因为要立即返还
            user.setDeposit(String.valueOf(finalDeposit));
            user.setWasPay(String.valueOf(finalDeposit));
            
            // 已平仓盈亏
            double currentWasIncome = Double.parseDouble(user.getWasIncome() != null ? user.getWasIncome() : "0");
            double inoutPrice = traderEntity.getInoutPrice() != null ? traderEntity.getInoutPrice().doubleValue() : 0;
            double newWasIncome = currentWasIncome + inoutPrice;
            user.setWasIncome(String.valueOf(newWasIncome));
            
            // 更新用户余额：只加上盈亏（隔夜费已在每天6点自动扣除）
            double newBalance = currentBalance + inoutPrice;
            
            // 检查新余额是否为负值
            if (newBalance < 0) {
                log.error("余额不足: 用户ID={}, 当前余额={}, 盈亏={}", traderDTO.getId(), currentBalance, inoutPrice);
                return Result.error(400, "余额不足，操作后余额不能为负值");
            }
            
            user.setLeftMoney(String.valueOf(newBalance));
            
            // 计算可用预付款 = 用户余额 - 已用预付款
            double finalCanPay = newBalance - finalDeposit;
            user.setCanPay(String.valueOf(finalCanPay));
            
            int userBalanceUpdateRows = userMapper.updateById(user);
            if (userBalanceUpdateRows <= 0) {
                log.error("更新用户账户失败: 用户ID={}", traderDTO.getId());
                return Result.error(500, "更新用户账户失败");
            }
        }
        
        // 插入数据库
        int rows = traderMapper.insert(traderEntity);
        
        if (rows > 0) {
            log.info("交易订单添加成功: 订单号={}", traderDTO.getOrderId());
            return Result.success(200, "交易订单添加成功");
        } else {
            log.error("添加交易订单失败: 订单号={}", traderDTO.getOrderId());
            return Result.error(500, "交易订单添加失败");
        }
    }

    // 统一查询使用 queryTraders(isOk, userId)

    /**
     * 通用查询：status=1；isOk=0/1 由前端传参控制
     */
    @Override
    public Result queryTraders(String isOk, String userId, Integer page, String traderSelect, Integer size) {
        if (isOk == null || isOk.isBlank()) {
            return Result.error(400, "参数isOk为必填，取值0或1");
        }
        if (!"0".equals(isOk) && !"1".equals(isOk)) {
            return Result.error(400, "参数isOk只能为0或1");
        }
        LambdaQueryWrapper<TraderEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(TraderEntity::getStatus, "1");
        qw.eq(TraderEntity::getIsOk, isOk);
        if(traderSelect != null) {
            qw.eq(TraderEntity::getTraderSelect, traderSelect);
        }
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
                TraderEntity::getInoutPrice,
                TraderEntity::getEntryExit,
                TraderEntity::getOvernightProportion,
                TraderEntity::getTraderSelect,
                TraderEntity::getTraderCloseSelect,
                TraderEntity::getScheduledTime,
                TraderEntity::getDeposit
        );
        qw.orderByDesc(TraderEntity::getOpeningTime);
        // 分页逻辑：如果提供了 size（>0），则启用分页；未提供 page 时默认 page=1
        if (size != null && size > 0) {
            long currentPage = (page == null || page <= 0) ? 1L : page.longValue();
            Page<TraderEntity> p = new Page<>(currentPage, size);
            Page<TraderEntity> resultPage = traderMapper.selectPage(p, qw);
            return Result.success(
                    Map.of(
                            "records", resultPage.getRecords(),
                            "total", resultPage.getTotal(),
                            "pages", resultPage.getPages(),
                            "current", resultPage.getCurrent(),
                            "size", resultPage.getSize()
                    )
            );
        }
        return Result.success(traderMapper.selectList(qw));
    }

    @Override
    public Result updateTrader(TraderDTO traderDTO) {
        // 基本校验：必须提供订单号用于更新
        if (traderDTO == null || traderDTO.getOrderId() == null || traderDTO.getOrderId().isBlank()) {
            return Result.error(400, "更新必须提供订单号orderId");
        }

        // 查询现有订单
        TraderEntity existingTrader = traderMapper.selectById(traderDTO.getOrderId());
        if (existingTrader == null) {
            return Result.error(404, "交易订单不存在");
        }

        // 选择性更新：仅对非空字段进行赋值，避免把未传字段覆盖为null
        if (traderDTO.getId() != null) existingTrader.setId(traderDTO.getId());
        if (traderDTO.getOpeningTime() != null) existingTrader.setOpeningTime(traderDTO.getOpeningTime());
        if (traderDTO.getClosingTime() != null) existingTrader.setClosingTime(traderDTO.getClosingTime());
        if (traderDTO.getDirection() != null) existingTrader.setDirection(traderDTO.getDirection());
        if (traderDTO.getVolume() != null) existingTrader.setVolume(traderDTO.getVolume());
        if (traderDTO.getVarieties() != null) existingTrader.setVarieties(traderDTO.getVarieties());
        if (traderDTO.getOpeningPrice() != null) existingTrader.setOpeningPrice(traderDTO.getOpeningPrice());
        if (traderDTO.getClosingPrice() != null) existingTrader.setClosingPrice(traderDTO.getClosingPrice());
        if (traderDTO.getEntryExit() != null) existingTrader.setEntryExit(traderDTO.getEntryExit());
        if (traderDTO.getOvernightProportion() != null) existingTrader.setOvernightProportion(traderDTO.getOvernightProportion());
        if (traderDTO.getStatus() != null) existingTrader.setStatus(traderDTO.getStatus());
        if (traderDTO.getScheduledTime() != null) existingTrader.setScheduledTime(traderDTO.getScheduledTime());
        if (traderDTO.getTraderCloseSelect() != null) existingTrader.setTraderCloseSelect(traderDTO.getTraderCloseSelect());

        // 派生字段计算：盈亏（inoutPrice）= (开仓价 - 平仓价) × 成交量
        try {
            BigDecimal openingPrice = existingTrader.getOpeningPrice();
            BigDecimal closingPrice = existingTrader.getClosingPrice();
            BigDecimal volume = existingTrader.getVolume(); // 盎司单位

            if (openingPrice != null && closingPrice != null && volume != null) {
                // 根据买卖方向计算盈亏：
                // 卖出(sell)：(开仓价 - 平仓价) × 成交量
                // 买多(buy)：(平仓价 - 开仓价) × 成交量
                String direction = existingTrader.getDirection();
                BigDecimal pnl = null;
                if (direction != null) {
                    if ("sell".equalsIgnoreCase(direction)) {
                        pnl = openingPrice.subtract(closingPrice).multiply(volume);
                    } else if ("buy".equalsIgnoreCase(direction)) {
                        pnl = closingPrice.subtract(openingPrice).multiply(volume);
                    }
                }
                // 仅当识别出方向时写入盈亏，保留两位小数
                if (pnl != null) {
                    pnl = pnl.setScale(2, RoundingMode.HALF_UP);
                    existingTrader.setInoutPrice(pnl);
                }
            }
        } catch (Exception e) {
            log.warn("更新派生字段计算异常: {}", e.getMessage());
        }

        // 当可选字段齐全（开仓价、平仓价、收盘价、平仓时间）时，自动置 isOk=1；否则保留原状态或按传参覆盖
        boolean optionalComplete = existingTrader.getOpeningPrice() != null
                && existingTrader.getClosingPrice() != null
                && existingTrader.getClosingTime() != null;
        if (optionalComplete) {
            existingTrader.setIsOk("1");
        } else if (traderDTO.getIsOk() != null) {
            // 如果显式传入isOk，则按传参设置
            existingTrader.setIsOk(traderDTO.getIsOk());
        }

        int rows = traderMapper.updateById(existingTrader);
        if (rows > 0) {
            UserEntity user = userMapper.selectById(existingTrader.getId());
            if (user != null && existingTrader.getVolume() != null && "1".equals(existingTrader.getIsOk())) {
                double depositAmount = existingTrader.getVolume().doubleValue() * 100;
                double currentDeposit = Double.parseDouble(user.getDeposit() != null ? user.getDeposit() : "0");
                double newDeposit = currentDeposit - depositAmount;
                // 已平仓盈亏
                double currentWasIncome = Double.parseDouble(user.getWasIncome() != null ? user.getWasIncome() : "0");
                double inoutPrice = existingTrader.getInoutPrice().doubleValue();
                double newWasIncome = currentWasIncome + inoutPrice;
                user.setWasIncome(String.valueOf(newWasIncome));

                // 更新用户余额：只加上盈亏（隔夜费已在每天6点自动扣除）
                double newLeftMoney = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0") + inoutPrice;
                
                // 检查新余额是否为负值
                if (newLeftMoney < 0) {
                    log.error("余额不足: 用户ID={}, 当前余额={}, 盈亏={}", user.getId(), user.getLeftMoney(), inoutPrice);
                    return Result.error(400, "余额不足，操作后余额不能为负值");
                }
                
                user.setLeftMoney(String.valueOf(newLeftMoney));
                
                if (newDeposit < 0) {
                    log.warn("扣除后用户保证金将为负值: 用户ID={}, 当前保证金={}, 扣除金额={}", user.getId(), currentDeposit, depositAmount);
                    newDeposit = 0; // 如果为负值则设为0
                }

                user.setDeposit(String.valueOf(newDeposit));
                // 已用预付款与保证金保持一致
                user.setWasPay(String.valueOf(newDeposit));
                // 计算可用预付款 = 用户余额 - 已用预付款
                double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
                double canPay = currentBalance - newDeposit;
                user.setCanPay(String.valueOf(canPay));
                int userDepositUpdateRows = userMapper.updateById(user);
                if (userDepositUpdateRows > 0) {
                    log.info("用户保证金已扣除: 用户ID={}, 扣除金额={}, 新保证金={}, 盈亏={}, 新余额={}", user.getId(), depositAmount, newDeposit, inoutPrice, newLeftMoney);
                } else {
                    log.error("扣除用户保证金失败: 用户ID={}", user.getId());
                    return Result.error(500, "更新用户账户失败");
                }
            }

            return Result.success(200, "交易订单更新成功");
        } else {
            log.error("更新交易订单失败: 订单号={}", traderDTO.getOrderId());
            return Result.error(500, "交易订单更新失败");
        }
    }

    @Override
    public Result updateNewTrader(TraderDTO traderDTO) {
        deleteTrader(traderDTO.getOrderId());
        Result s =addTrader(traderDTO);
        return (s.getCode() == 200) ? Result.success(200,"成功更新"):Result.error(500,"删除失败");
    }

    @Override
    public Result deleteTrader(String orderId) {
        // 检查交易订单是否存在
        TraderEntity existingTrader = traderMapper.selectById(orderId);
        if (existingTrader == null) {
            return Result.error(404, "交易订单不存在");
        }
        
        // 如果是balance类型订单或订单已完成（isOk=1），需要回滚资金
        if ("1".equals(existingTrader.getIsOk()) && !"balance".equals(existingTrader.getDirection())) {
            UserEntity user = userMapper.selectById(existingTrader.getId());
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            // 1. 回滚盈亏：从余额和已平仓盈亏中减去
            double inoutPrice = existingTrader.getInoutPrice() != null ? existingTrader.getInoutPrice().doubleValue() : 0;
            double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
            double currentWasIncome = Double.parseDouble(user.getWasIncome() != null ? user.getWasIncome() : "0");
            double currentWasPay = Double.parseDouble(user.getWasPay() != null ? user.getWasPay() : "0");

            double newBalance = currentBalance - inoutPrice;
            double newWasIncome = currentWasIncome - inoutPrice;
            
            // 2. 回滚隔夜费：从余额中减去（隔夜费已经添加到余额）
            double overnightFee = existingTrader.getOvernightPrice() != null ? existingTrader.getOvernightPrice().doubleValue() : 0;
            newBalance = newBalance - overnightFee;
            
            // 3. 回滚保证金：恢复保证金到用户账户
//            double depositAmount = existingTrader.getVolume() != null ? existingTrader.getVolume().doubleValue() * 100 : 0;
//            double currentDeposit = Double.parseDouble(user.getDeposit() != null ? user.getDeposit() : "0");
//            double newDeposit = currentDeposit + depositAmount;
            
            // 检查余额是否为负
            if (newBalance < 0) {
                log.error("删除订单后余额不足: 用户ID={}, 当前余额={}, 盈亏={}, 隔夜费={}", user.getId(), currentBalance, inoutPrice, overnightFee);
                return Result.error(400, "删除订单后余额不足，无法删除");
            }
            
            // 更新用户账户
            user.setLeftMoney(String.valueOf(newBalance));
            user.setWasIncome(String.valueOf(newWasIncome));
//            user.setDeposit(String.valueOf(newDeposit));
//            user.setWasPay(String.valueOf(newDeposit));
//
            // 重新计算可用预付款
            double canPay = newBalance - currentWasPay;
            user.setCanPay(String.valueOf(canPay));
            
            int userUpdateRows = userMapper.updateById(user);
            if (userUpdateRows <= 0) {
                log.error("回滚用户账户失败: 用户ID={}", user.getId());
                return Result.error(500, "回滚用户账户失败");
            }
        } else if ("balance".equals(existingTrader.getDirection())) {
            // balance类型订单：回滚出入金
            UserEntity user = userMapper.selectById(existingTrader.getId());
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            
            double entryExitAmount = existingTrader.getEntryExit() != null ? existingTrader.getEntryExit().doubleValue() : 0;
            double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
            double newBalance = currentBalance - entryExitAmount;
            
            if (newBalance < 0) {
                log.error("删除出入金订单后余额不足: 用户ID={}, 当前余额={}, 出入金金额={}", user.getId(), currentBalance, entryExitAmount);
                return Result.error(400, "删除订单后余额不足，无法删除");
            }
            
            user.setLeftMoney(String.valueOf(newBalance));
            
            // 重新计算可用预付款
            double wasPay = Double.parseDouble(user.getWasPay() != null ? user.getWasPay() : "0");
            double canPay = newBalance - wasPay;
            user.setCanPay(String.valueOf(canPay));
            
            int userUpdateRows = userMapper.updateById(user);
            if (userUpdateRows <= 0) {
                log.error("回滚用户余额失败: 用户ID={}", user.getId());
                return Result.error(500, "回滚用户余额失败");
            }
            
            log.info("已回滚出入金: 用户ID={}, 金额={}, 新余额={}", user.getId(), entryExitAmount, newBalance);
        }
        

        int rows = traderMapper.deleteById(existingTrader);
        
        if (rows > 0) {
            log.info("交易订单删除成功: 订单号={}", orderId);
            return Result.success(200, "交易订单删除成功");
        } else {
            log.error("删除交易订单失败: 订单号={}", orderId);
            return Result.error(500, "交易订单删除失败");
        }
    }
}
