package com.gold.goldsystem.controller;

import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @PostMapping("/save")
    public Result saveOrUpdatePrice(@RequestBody PriceDTO priceDTO) {
        return priceService.saveOrUpdatePrice(priceDTO);
    }
}