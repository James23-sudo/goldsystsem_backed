package com.gold.goldsystem.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@TableName(value ="user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserEntity {
    private String id;
    private String remark;
    private String leftMoney;
    private String value;
    private String deposit;
    private String canPay;
    private String wasPay;
    private String wasIncome;
    private String havingIncome;
    private String totalIncome;
}
