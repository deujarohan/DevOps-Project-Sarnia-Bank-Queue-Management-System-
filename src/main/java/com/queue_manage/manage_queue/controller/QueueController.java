package com.queue_manage.manage_queue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.queue_manage.manage_queue.model.*;
import com.queue_manage.manage_queue.service.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/queue")
@CrossOrigin(origins = "*") // Allow any website to call this
public class QueueController {

    @Autowired
    private QueueService queueService;

    // Customer joins the queue
    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(@RequestBody Map<String, String> request) {
        String name = request.get("customerName");
        String serviceTypeStr = request.get("serviceType");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Customer name is required"));
        }

        QueueEntry.ServiceType serviceType;
        try {
            serviceType = QueueEntry.ServiceType.valueOf(serviceTypeStr);
        } catch (Exception e) {
            serviceType = QueueEntry.ServiceType.GENERAL;
        }

        QueueEntry entry = queueService.joinQueue(name.trim(), serviceType);
        return ResponseEntity.ok(entry);
    }

    // Get current waiting queue
    @GetMapping("/status")
    public ResponseEntity<List<QueueEntry>> getQueueStatus() {
        return ResponseEntity.ok(queueService.getWaitingQueue());
    }

    // Get full queue including served/skipped
    @GetMapping("/all")
    public ResponseEntity<List<QueueEntry>> getAllEntries() {
        return ResponseEntity.ok(queueService.getFullQueue());
    }

    // Get ticket by ID
    @GetMapping("/ticket/{id}")
    public ResponseEntity<?> getTicket(@PathVariable Long id) {
        Optional<QueueEntry> entry = queueService.getTicketById(id);
        if (entry.isPresent()) {
            return ResponseEntity.ok(entry.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Get currently serving
    @GetMapping("/serving")
    public ResponseEntity<?> getCurrentlyServing() {
        Optional<QueueEntry> serving = queueService.getCurrentlyServing();
        return serving.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.noContent().build());
    }

    // Call next customer (admin)
    @PostMapping("/next")
    public ResponseEntity<?> callNext() {
        Optional<QueueEntry> next = queueService.callNext();
        if (next.isPresent()) {
            return ResponseEntity.ok(next.get());
        }
        return ResponseEntity.ok(Map.of("message", "Queue is empty"));
    }

    // Mark customer as served (admin)
    @PutMapping("/serve/{id}")
    public ResponseEntity<?> serveCustomer(@PathVariable Long id) {
        Optional<QueueEntry> entry = queueService.serveCustomer(id);
        if (entry.isPresent()) {
            return ResponseEntity.ok(entry.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Skip customer (admin)
    @PutMapping("/skip/{id}")
    public ResponseEntity<?> skipCustomer(@PathVariable Long id) {
        Optional<QueueEntry> entry = queueService.skipCustomer(id);
        if (entry.isPresent()) {
            return ResponseEntity.ok(entry.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Get stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(queueService.getQueueStats());
    }

    // Clear queue (admin)
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearQueue() {
        queueService.clearQueue();
        return ResponseEntity.ok(Map.of("message", "Queue cleared successfully"));
    }
}

