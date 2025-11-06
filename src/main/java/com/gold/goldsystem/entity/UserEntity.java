package com.gold.goldsystem.entity;

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
    private String left_money;
    private String value;
    private String deposit;
    private String can_pay;
    private String was_pay;
    private String was_income;
    private String having_income;
    private String total_income;
}
