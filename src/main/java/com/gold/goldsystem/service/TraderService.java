package com.gold.goldsystem.service;

import com.gold.goldsystem.dto.TraderDTO;
import com.gold.goldsystem.entity.Result;

public interface TraderService {

    /**
     * 新增交易订单
     * @param traderDTO 交易订单信息
     * @return 新增结果
     */
    Result addTrader(TraderDTO traderDTO);


    /**
     * 根据用户ID查询交易订单列表
     * @param userId 用户ID
     * @return 查询结果
     */
    Result getTradersByUserId(String userId);

    /**
     * 更新交易订单
     * @param traderDTO 交易订单信息
     * @return 更新结果
     */
    Result updateTrader(TraderDTO traderDTO);

    /**
     * 删除交易订单（逻辑删除，将status设为0）
     * @param orderId 订单号
     * @return 删除结果
     */
    Result deleteTrader(String orderId);
}
