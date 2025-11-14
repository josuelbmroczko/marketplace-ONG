package com.marketplace.marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String pageHome(){
        return "home";
    }

    @GetMapping("/login")
    public String pageLogin(){
        return "login";
    }

}