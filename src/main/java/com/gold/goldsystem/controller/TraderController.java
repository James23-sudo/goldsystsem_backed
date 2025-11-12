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
     * 更新交易订单（用于二次填数据：开仓价、平仓价、收盘价、平仓时间等可选字段）
     * 根据订单号更新；服务层将计算隔夜费与盈亏，并在可选字段齐全时置 isOk=1
     */
    @PostMapping("/updateTrader")
    public Result updateTrader(@RequestBody TraderDTO traderDTO) {
        log.info("收到更新交易订单请求: {}", traderDTO);
        return traderService.updateTrader(traderDTO);
    }
    /**
     * 更新交易订单（用于二次填数据：开仓价、平仓价、收盘价、平仓时间等可选字段）
     * 根据订单号更新；服务层将计算隔夜费与盈亏，并在可选字段齐全时置 isOk=1
     */
    @PostMapping("/updateNewTrader")
    public Result updateNewTrader(@RequestBody TraderDTO traderDTO) {
        log.info("收到更新交易订单请求: {}", traderDTO);
        return traderService.updateNewTrader(traderDTO);
    }

    /**
     * 逻辑删除交易订单：将 status 设为 0（无效）
     * 路由示例：DELETE /api/trader/deleteTrader/{orderId}
     */
    @DeleteMapping("/deleteTrader/{orderId}")
    public Result deleteTrader(@PathVariable String orderId) {
        // 中文注释：逻辑删除不会移除数据，只将 status 更新为 0
        log.info("收到逻辑删除交易订单请求: {}", orderId);
        return traderService.deleteTrader(orderId);
    }

    /**
     * 查询接口：status=1 固定；isOk=0/1 由前端传参控制
     * 示例：/api/trader/list?isOk=0 或 /api/trader/list?isOk=1&userId=123456
     */
    @GetMapping("/list")
    public Result list(@RequestParam("isOk") String isOk,
                       @RequestParam(value = "userId", required = false) String userId,
                       @RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "traderSelect", required = false) String traderSelect,
                       @RequestParam(value = "isOpen", required = false) String isOpen,
                       @RequestParam(value = "size", required = false) Integer size) {
        // 中文注释：分页为可选参数；不传则返回全部列表
        return traderService.queryTraders(isOk, userId, page, traderSelect, isOpen, size);
    }
    
}
