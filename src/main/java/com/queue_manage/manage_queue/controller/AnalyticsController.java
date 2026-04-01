package com.queue_manage.manage_queue.controller;

import com.queue_manage.manage_queue.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private QueueService queueService;

    // GET /api/analytics/summary
    // Returns full analytics: totals, avg wait, service breakdown, recent entries
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(queueService.getAnalytics());
    }

    // GET /api/analytics/stats
    // Returns quick live stats: waiting, serving, served, skipped counts
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(queueService.getQueueStats());
    }
}