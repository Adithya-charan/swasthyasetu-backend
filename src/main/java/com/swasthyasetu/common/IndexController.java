package com.swasthyasetu.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    
    @GetMapping("/")
    public String index() {
        return "Welcome to SwasthyaSetu API Server! The backend is up and running.";
    }
}
