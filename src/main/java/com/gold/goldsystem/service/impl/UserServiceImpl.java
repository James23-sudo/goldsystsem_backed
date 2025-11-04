package com.gold.goldsystem.service.impl;

import com.gold.goldsystem.dto.UserDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.entity.UserEntity;
import com.gold.goldsystem.mapper.UserMapper;
import com.gold.goldsystem.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
