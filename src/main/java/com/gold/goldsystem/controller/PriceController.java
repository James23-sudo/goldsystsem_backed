package com.gold.goldsystem.controller;

import com.gold.goldsystem.dto.PriceDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    /**
     * 查询价格数据接口
     * 可选传入 priceDate（yyyy-MM-dd） 与 isSelectAm（"1"=上午，"0"=下午）
     * 不传参数则返回全部 price 数据
     */
    @GetMapping("/list")
    public Result listPrices(@RequestParam(value = "priceDate", required = false) String priceDate,
                             @RequestParam(value = "isSelectAm", required = false) String isSelectAm) {

        return priceService.queryPrices(priceDate, isSelectAm);
    }
}