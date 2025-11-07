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

@TableName(value = "price")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PriceEntity {
    @TableId
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime priceDate;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String isSelectAm;
}