package com.gold.goldsystem.controller;

import com.gold.goldsystem.dto.UserDTO;
import com.gold.goldsystem.entity.Result;
import com.gold.goldsystem.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    @Resource
    private UserService userService;
    
    /**
     * Add user
     * @param userDTO user data transfer object
     * @return result of adding user
     */
    @PostMapping("/add")
    public Result addUser(@RequestBody UserDTO userDTO) {
        log.info("Received request to add user: {}", userDTO);
        return userService.addUser(userDTO);
    }

    /**
     * 查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable String id) {
        log.info("Received request to get user by id: {}", id);
        return userService.getUserById(id);
    }

    /**
     * 查询所有用户信息
     * @return
     */
    @GetMapping("")
    public Result getUser(){
        log.info("Received request to get user");
        return userService.getUser();
    }

    @GetMapping("/list")
    public Result listUsers(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        log.info("Received request to list users with pagination: page={}, size={}", page, size);
        return userService.listUsers(page, size);
    }

}
