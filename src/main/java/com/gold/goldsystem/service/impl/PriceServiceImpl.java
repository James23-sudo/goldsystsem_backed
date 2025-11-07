package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.PriceEntity;
import com.gold.goldsystem.mapper.PriceMapper;
import com.gold.goldsystem.service.PriceService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriceServiceImpl implements PriceService {

    @Autowired
    private PriceMapper priceMapper;

    @Override
    public void saveOrUpdatePrice(PriceDTO priceDTO) {
        PriceEntity existingPrice = priceMapper.selectOne(new QueryWrapper<PriceEntity>().eq("price_date", priceDTO.getPriceDate()));

        if (existingPrice != null) {
            // 更新
            BeanUtils.copyProperties(priceDTO, existingPrice);
            priceMapper.updateById(existingPrice);
        } else {
            // 插入
            PriceEntity newPrice = new PriceEntity();
            BeanUtils.copyProperties(priceDTO, newPrice);
            priceMapper.insert(newPrice);
        }
    }
}