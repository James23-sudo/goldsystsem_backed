package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.PriceEntity;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.mapper.PriceMapper;
import com.gold.goldsystem.entity.TraderEntity;
import com.gold.goldsystem.mapper.TraderMapper;
import com.gold.goldsystem.service.PriceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceServiceImpl implements PriceService {

    @Autowired
    private PriceMapper priceMapper;

    @Autowired
    private TraderMapper traderMapper;

    @Override
    public Result saveOrUpdatePrice(PriceDTO priceDTO) {
        // 修正：由于 price 表是日期+am/pm的组合主键，查询时必须同时提供两个条件
        PriceEntity existingPrice = priceMapper.selectOne(new QueryWrapper<PriceEntity>()
                .eq("price_date", priceDTO.getPriceDate())
                .eq("is_select_am", priceDTO.getIsSelectAm()));

        PriceEntity savedEntity;
        if (existingPrice != null) {
            // 2. 存在则更新：仅更新非主键字段（buy_price、sell_price），并通过组合主键锁定记录
            PriceEntity updateEntity = new PriceEntity();
            updateEntity.setBuyPrice(priceDTO.getBuyPrice());
            updateEntity.setSellPrice(priceDTO.getSellPrice());

            priceMapper.update(updateEntity, new UpdateWrapper<PriceEntity>()
                    .eq("price_date", priceDTO.getPriceDate())  // 组合主键条件1
                    .eq("is_select_am", priceDTO.getIsSelectAm()));  // 组合主键条件2
            savedEntity = updateEntity;
        } else {
            // 数据库不存在该记录，执行插入
            PriceEntity newPrice = new PriceEntity();
            BeanUtils.copyProperties(priceDTO, newPrice);
            priceMapper.insert(newPrice);
            savedEntity = newPrice;
        }

        // --- 新增逻辑：保存定价后，自动填充待处理订单的开仓价格 ---

        // 1. 根据本次定价是上午(1)还是下午(0)，确定要匹配的订单类型("am"或"pm")
        String traderSelect = "1".equals(priceDTO.getIsSelectAm()) ? "am" : "pm";

        // 2. 查询所有【开仓价为空】且【交易时段匹配】的待处理订单
        QueryWrapper<TraderEntity> tradeQuery = new QueryWrapper<>();
        tradeQuery.isNull("opening_price"); // 条件一：开仓价未填写
        tradeQuery.eq("trader_select", traderSelect);   // 条件二：交易时段(am/pm)匹配
        List<TraderEntity> pendingTrades = traderMapper.selectList(tradeQuery);

        // 3. 遍历所有待处理订单，根据买卖方向计算并填充开仓价格
        for (TraderEntity trade : pendingTrades) {
            BigDecimal openingPrice = null;
            String direction = trade.getDirection();

            if ("buy".equalsIgnoreCase(direction)) {
                // 买入方向的订单：开仓价 = 本次定价的买入价 + 0.15
                openingPrice = priceDTO.getBuyPrice().add(new BigDecimal("0.15"));
            } else if ("sell".equalsIgnoreCase(direction)) {
                // 卖出方向的订单：开仓价 = 本次定价的卖出价 - 0.15
                openingPrice = priceDTO.getSellPrice().subtract(new BigDecimal("0.15"));
            }

            // 如果是buy或sell订单（排除了balance等其他类型），则更新数据库中的开仓价格
            if (openingPrice != null) {
                trade.setOpeningPrice(openingPrice);
                traderMapper.updateById(trade);
            }
        }
        // --- 新增逻辑结束 ---

        // 返回保存后的价格信息给前端
        return Result.success(savedEntity);
    }
}