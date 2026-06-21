package com.yourname.ticketflow.modules.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 内置 API 文档页面（不依赖 SpringDoc/Knife4j）
 */
@Controller
public class ApiDocController {

    @GetMapping("/docs")
    public String docs() {
        return "redirect:/api-docs.html";
    }
}
