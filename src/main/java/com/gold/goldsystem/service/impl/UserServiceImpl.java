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
}
