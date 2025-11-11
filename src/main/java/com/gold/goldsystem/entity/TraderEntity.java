package com.gold.goldsystem.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "trader")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TraderEntity {
    /**
     * 账号（外键，关联用户表id）
     */
    private String id;
    
    /**
     * 订单号（主键）
     */
    @TableId
    private String orderId;
    
    /**
     * 开仓时间
     */
    private LocalDateTime openingTime;
    
    /**
     * 平仓时间
     */
    private LocalDateTime closingTime;

    /**
     * 预定时间
     */
    private LocalDateTime scheduledTime;
    
    /**
     * 买卖方向
     */
    private String direction;
    
    /**
     * 成交量
     */
    private BigDecimal volume;
    
    /**
     * 交易品种
     */
    private String varieties;
    
    /**
     * 开仓价格
     */
    private BigDecimal openingPrice;
    
    /**
     * 平仓价格
     */
    private BigDecimal closingPrice;
    
    /**
     * 隔夜费
     */
    private BigDecimal overnightPrice;
    
    /**
     * 盈亏
     */
    private BigDecimal inoutPrice;
    
    /**
     * 是否已处理（1=是，0=否）
     */
    private String isOk;
    
    /**
     * 是否有效（1=有效，0=无效）
     */
    private String status;
    
    /**
     * 收盘价
     */
    private BigDecimal overPrice;
    
    /**
     * 出入金
     */
    private BigDecimal entryExit;
    
    /**
     * 隔夜费比例
     */
    private BigDecimal overnightProportion;

    /**
     * am\pm
     */
    private String traderSelect;

    /**
     *  保证金
     */
    private BigDecimal deposit;

    /**
     *  am\pm
     */
    private String traderCloseSelect;
}
