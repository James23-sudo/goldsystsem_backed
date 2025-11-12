package com.gold.goldsystem.service;

import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.Result;

public interface PriceService {
    Result saveOrUpdatePrice(PriceDTO priceDTO);

    /**
     * 查询价格数据（可选条件：日期、上午/下午）
     * @param priceDate 日期，格式 yyyy-MM-dd，可为空
     * @param isSelectAm "1"=上午，"0"=下午，可为空
     * @return 查询结果
     */
    Result queryPrices(String priceDate, String isSelectAm);
}