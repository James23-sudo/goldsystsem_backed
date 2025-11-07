package com.gold.goldsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String priceDate;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String isSelectAm;
}