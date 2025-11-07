package com.gold.goldsystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceDTO {
    private LocalDateTime priceDate;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String isSelectAm;
}