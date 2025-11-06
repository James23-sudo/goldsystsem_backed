package com.gold.goldsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserDTO {
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
