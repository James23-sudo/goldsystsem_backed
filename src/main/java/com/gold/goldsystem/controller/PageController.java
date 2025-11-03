package com.gold.goldsystem.controller;

import com.gold.goldsystem.entity.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
public class PageController {
    /**
     * 简单的测试接口，用于验证系统是否正常运行
     * @return 返回成功消息
     */
    @GetMapping("/hello")
    public Result hello() {
        log.info("测试接口被调用");
        return Result.success("Hello, 系统运行正常！");
    }

    /**
     * 带参数的测试接口
     * @param name 姓名参数
     * @return 返回问候消息
     */
    @GetMapping("/greet")
    public Result greet(@RequestParam(defaultValue = "World") String name) {
        log.info("问候接口被调用，参数: {}", name);
        return Result.success("Hello, " + name + "!");
    }
}
