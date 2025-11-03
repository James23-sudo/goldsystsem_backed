package com.gold.goldsystem.controller;

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
    
}
