package com.xjl.controller;

import com.xjl.domain.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    public R<String> hello() {
        return R.ok("hello world");
    }
}
