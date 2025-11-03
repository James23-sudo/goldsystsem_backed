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
public class PageEntity {
    private Integer userId;
}
