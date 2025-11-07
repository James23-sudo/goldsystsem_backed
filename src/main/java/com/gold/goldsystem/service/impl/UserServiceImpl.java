package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gold.goldsystem.dto.UserDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.UserMapper;
import com.gold.goldsystem.entity.TraderEntity;
import com.gold.goldsystem.mapper.TraderMapper;
import com.gold.goldsystem.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private TraderMapper traderMapper;

    @Override
    public Result addUser(UserDTO userDTO) {
        // Check if user ID already exists
        UserEntity existingUser = userMapper.selectById(userDTO.getId());
        if (existingUser != null) {
            return Result.error(400, "用户账号已存在!");
        }
        
        // Convert DTO to Entity
        UserEntity userEntity = new UserEntity()
                .setId(userDTO.getId())
                .setRemark(userDTO.getRemark());
        
        // Insert into database
        int rows = userMapper.insert(userEntity);
        
        if (rows > 0) {
            log.info("User added successfully: {}", userDTO.getId());
            return Result.success(200, "User added successfully");
        } else {
            log.error("Failed to add user: {}", userDTO.getId());
            return Result.error(500, "Failed to add user");
        }
    }

    /**
     * 查询所有用户列表
     */
    @Override
    public Result getUser() {
        List<UserEntity> users = userMapper.selectList(null);
        return Result.success(users);
    }

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 查询结果
     */
    @Override
    public Result getUserById(String id) {
        // 参数校验：避免空ID
        if (id == null || id.isBlank()) {
            return Result.error(400, "用户ID不能为空");
        }

        // 通过主键查询用户
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 查询成功，返回用户信息
        return Result.success(user);
    }

    @Override
    public Result listUsers(Integer page, Integer size) {
        // 准备分页查询
        Page<UserEntity> p = new Page<>((page == null || page <= 0) ? 1L : page.longValue(), size.longValue());
        Page<UserEntity> resultPage = userMapper.selectPage(p, new QueryWrapper<>());

        // 遍历查询到的每个用户，为其计算并填充余额
        for (UserEntity user : resultPage.getRecords()) {
            // 1. 获取该用户所有的交易记录
            List<TraderEntity> trades = traderMapper.selectList(new QueryWrapper<TraderEntity>().eq("id", user.getId()));

            // 2. 初始化计算变量
            BigDecimal totalDepositWithdrawal = BigDecimal.ZERO; // 出入金总和
            BigDecimal totalProfitLoss = BigDecimal.ZERO;       // 盈亏总和
            BigDecimal totalOvernightFee = BigDecimal.ZERO;     // 隔夜费总和

            // 3. 遍历所有交易记录，分类累加
            for (TraderEntity trade : trades) {
                // 累加出入金（无论订单是否完成）
                if ("balance".equalsIgnoreCase(trade.getDirection()) && trade.getInoutPrice() != null) {
                    totalDepositWithdrawal = totalDepositWithdrawal.add(trade.getInoutPrice());
                }

                // 只计算已完成订单（is_ok = '1'）的盈亏和隔夜费
                if ("1".equals(trade.getIsOk())) {
                    // 修正：根据用户要求，已完成订单的盈亏来自于 inout_price 字段
                    if (trade.getInoutPrice() != null) {
                        totalProfitLoss = totalProfitLoss.add(trade.getInoutPrice());
                    }
                    if (trade.getOvernightPrice() != null) {
                        totalOvernightFee = totalOvernightFee.add(trade.getOvernightPrice());
                    }
                }
            }

            // 4. 根据公式计算最终余额
            // 余额 = 出入金总和 + 盈亏总和 - 隔夜费总和
            BigDecimal balance = totalDepositWithdrawal.add(totalProfitLoss).subtract(totalOvernightFee);

            // 5. 将计算出的余额（保留两位小数）设置到用户的 leftMoney 字段
            user.setLeftMoney(balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            // 6. 新增：将更新后的用户信息（包含余额）保存回数据库
            userMapper.updateById(user);
        }

        // 7. 返回带有余额信息的分页结果
        return Result.success(
                Map.of(
                        "records", resultPage.getRecords(),
                        "total", resultPage.getTotal(),
                        "pages", resultPage.getPages(),
                        "current", resultPage.getCurrent(),
                        "size", resultPage.getSize()
                )
        );
    }
}
