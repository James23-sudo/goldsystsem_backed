package com.gold.goldsystem.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gold.goldsystem.entity.TraderEntity;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.TraderMapper;
import com.gold.goldsystem.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 隔夜费定时任务
 * 每天凌晨6点自动扣除未完成订单的隔夜费
 */
@Component
@Slf4j
public class OvernightFeeScheduledTask {

    @Autowired
    private TraderMapper traderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 每天早上6点执行隔夜费扣除
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void deductOvernightFee() {
        log.info("开始执行隔夜费扣除任务: {}", LocalDateTime.now());

        try {
            // 获取昨天的日期
            LocalDate yesterday = LocalDate.now().minusDays(1);
            DayOfWeek dayOfWeek = yesterday.getDayOfWeek();

            // 获取隔夜费倍数
            int multiplier = getOvernightMultiplier(dayOfWeek);

            if (multiplier == 0) {
                log.info("昨天是{}，不收取隔夜费", dayOfWeek);
                return;
            }

            log.info("昨天是{}，隔夜费倍数={}", dayOfWeek, multiplier);

            // 查询所有未完成的交易订单（isOk=0, status=1,isOpen=1）
            QueryWrapper<TraderEntity> query = new QueryWrapper<>();
            query.eq("is_ok", "0");
            query.eq("status", "1");
            query.eq("is_open", "1");
            query.isNotNull("opening_price");
            query.isNotNull("volume");
            query.isNotNull("overnight_proportion");
            List<TraderEntity> pendingTrades = traderMapper.selectList(query);

            log.info("找到 {} 个待处理订单需要扣除隔夜费", pendingTrades.size());

            // 按用户ID分组统计隔夜费
            Map<String, BigDecimal> userOvernightFees = new HashMap<>();

            for (TraderEntity trade : pendingTrades) {
                // 计算单个订单的每日隔夜费基数
                BigDecimal openingPrice = trade.getOpeningPrice();
                BigDecimal volume = trade.getVolume();
                BigDecimal overnightProportion = trade.getOvernightProportion();

                BigDecimal lots = volume.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP);
                BigDecimal dailyFeeBase = openingPrice
                        .multiply(lots)
                        .multiply(overnightProportion)
                        .divide(new BigDecimal("360"), 8, RoundingMode.HALF_UP);

                // 应用倍数
                BigDecimal dailyFee = dailyFeeBase.multiply(new BigDecimal(multiplier))
                        .setScale(2, RoundingMode.HALF_UP);

                // 累加到该用户的总隔夜费
                String userId = trade.getId();
                BigDecimal todayOvernightFee = userOvernightFees.merge(userId, dailyFee, BigDecimal::add);
                trade.setOvernightPrice(trade.getOvernightPrice().add(todayOvernightFee));
                traderMapper.updateById(trade);
                log.debug("订单{}隔夜费: userId={}, fee={}", trade.getOrderId(), userId, dailyFee);
            }

            // 批量更新用户余额
            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<String, BigDecimal> entry : userOvernightFees.entrySet()) {
                String userId = entry.getKey();
                BigDecimal totalFee = entry.getValue();

                UserEntity user = userMapper.selectById(userId);
                if (user == null) {
                    log.warn("用户不存在: userId={}", userId);
                    failCount++;
                    continue;
                }

                // 更新用户余额：加上隔夜费（隔夜费可能为正也可能为负）
                double currentBalance = Double.parseDouble(user.getLeftMoney() != null ? user.getLeftMoney() : "0");
                double newBalance = currentBalance + totalFee.doubleValue();

                user.setLeftMoney(String.valueOf(newBalance));

                // 重新计算可用预付款
                double wasPay = Double.parseDouble(user.getWasPay() != null ? user.getWasPay() : "0");
                double canPay = newBalance - wasPay;
                user.setCanPay(String.valueOf(canPay));

                int rows = userMapper.updateById(user);
                if (rows > 0) {
                    log.info("隔夜费已扣除: userId={}, fee={}, 原余额={}, 新余额={}", userId, totalFee, currentBalance, newBalance);
                    successCount++;
                } else {
                    log.error("隔夜费扣除失败: userId={}", userId);
                    failCount++;
                }
            }

            log.info("隔夜费扣除任务完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("隔夜费扣除任务执行异常", e);
        }
    }

    /**
     * 获取隔夜费倍数
     * 周一、二、四、五: 1倍
     * 周三: 3倍
     * 周六、日: 0倍（不收费）
     */
    private int getOvernightMultiplier(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
            case TUESDAY:
            case THURSDAY:
            case FRIDAY:
                return 1;
            case WEDNESDAY:
                return 3;
            default:
                return 0; // 周六、周日不收费
        }
    }
}
