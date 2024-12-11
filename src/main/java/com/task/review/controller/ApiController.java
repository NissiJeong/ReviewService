package com.task.review.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class ApiController {
    @GetMapping("/test")
    public ResponseEntity<?> testApi() {
        log.info("success");
        return ResponseEntity.ok("success");
    }
}
