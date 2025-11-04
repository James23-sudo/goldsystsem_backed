package com.gold.goldsystem.service;

import com.gold.goldsystem.dto.UserDTO;
import com.gold.goldsystem.entity.Result;

public interface UserService {

    /**
     * 更新
     * @param userDTO 文件
     * @return 更新结果
     */
    Result addUser(UserDTO userDTO);

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 查询结果
     */
    Result getUserById(String id);

    /**
     * 查询所有用户列表
     * @return 用户列表
     */
    Result getUser();

}
