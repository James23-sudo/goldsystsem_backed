package com.gold.goldsystem.service;

import com.gold.goldsystem.dto.PriceDTO;

public interface PriceService {
    void saveOrUpdatePrice(PriceDTO priceDTO);
}