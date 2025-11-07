package com.gold.goldsystem.service;

import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.Result;

public interface PriceService {
    Result saveOrUpdatePrice(PriceDTO priceDTO);
}