package com.gold.goldsystem.controller;

import com.gold.goldsystem.dto.TraderDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.service.TraderService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trader")
@Slf4j
@RequiredArgsConstructor
public class TraderController {
    
    @Resource
    private TraderService traderService;

    /**
     * 添加交易订单（必填字段：出入金、买卖方向、成交量、隔夜费比例、开仓时间、客户账号及订单号）
     * 可选字段：开仓价、平仓价、收盘价、平仓时间、交易品种等
     */
    @PostMapping("/addTrader")
    public Result addTrader(@RequestBody TraderDTO traderDTO) {
        log.info("收到添加交易订单请求: {}", traderDTO);
        return traderService.addTrader(traderDTO);
    }

    /**
     * 查询接口：status=1 固定；isOk=0/1 由前端传参控制
     * 示例：/api/trader/list?isOk=0 或 /api/trader/list?isOk=1&userId=123456
     */
    @GetMapping("/list")
    public Result list(@RequestParam("isOk") String isOk,
                                                  @RequestParam(value = "userId", required = false) String userId) {
        return traderService.queryTraders(isOk, userId);
    }
    
}
