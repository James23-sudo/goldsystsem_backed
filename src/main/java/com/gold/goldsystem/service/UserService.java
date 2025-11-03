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
}
