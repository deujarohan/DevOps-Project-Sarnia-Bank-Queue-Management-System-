package com.queue_manage.manage_queue.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class PageController {

    @GetMapping(value = "/admin", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> adminPage() throws IOException {
        Resource resource = new ClassPathResource("static/admin.html");
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }
}