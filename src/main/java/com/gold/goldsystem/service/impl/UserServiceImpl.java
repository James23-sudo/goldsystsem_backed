package com.gold.goldsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gold.goldsystem.dto.UserDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.UserMapper;
import com.gold.goldsystem.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

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
        if (size != null && size > 0) {
            long currentPage = (page == null || page <= 0) ? 1L : page.longValue();
            Page<UserEntity> p = new Page<>(currentPage, size);
            Page<UserEntity> resultPage = userMapper.selectPage(p, new QueryWrapper<>());
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
        return Result.success(userMapper.selectList(null));
    }
}
