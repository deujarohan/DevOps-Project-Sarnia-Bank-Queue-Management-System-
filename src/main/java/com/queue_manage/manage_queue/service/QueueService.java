package com.queue_manage.manage_queue.service;


import com.queue_manage.manage_queue.model.*;
import com.queue_manage.manage_queue.repository.*;
import com.queue_manage.manage_queue.model.QueueEntry.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class QueueService {

    private static final int MINUTES_PER_CUSTOMER = 5;
    private static int ticketCounter = 1;

    @Autowired
    private QueueRepository queueRepository;

    public QueueEntry joinQueue(String customerName, QueueEntry.ServiceType serviceType) {
        QueueEntry entry = new QueueEntry();
        entry.setCustomerName(customerName);
        entry.setServiceType(serviceType);
        entry.setTicketNumber(generateTicketNumber(serviceType));
        entry.setStatus(Status.WAITING);

        QueueEntry saved = queueRepository.save(entry);

        // Update position and estimated wait
        updatePositions();
        saved = queueRepository.findById(saved.getId()).orElse(saved);
        return saved;
    }

    public List<QueueEntry> getWaitingQueue() {
        return queueRepository.findByStatusOrderByJoinedAtAsc(Status.WAITING);
    }

    public List<QueueEntry> getFullQueue() {
        return queueRepository.findAllByOrderByJoinedAtAsc();
    }

    public Optional<QueueEntry> getTicketById(Long id) {
        return queueRepository.findById(id);
    }

    public Optional<QueueEntry> callNext() {
        Optional<QueueEntry> next = queueRepository.findFirstByStatusOrderByJoinedAtAsc(Status.WAITING);
        next.ifPresent(entry -> {
            entry.setStatus(Status.SERVING);
            queueRepository.save(entry);
            updatePositions();
        });
        return next;
    }

    public Optional<QueueEntry> serveCustomer(Long id) {
        Optional<QueueEntry> entry = queueRepository.findById(id);
        entry.ifPresent(e -> {
            e.setStatus(Status.SERVED);
            e.setServedAt(LocalDateTime.now());
            queueRepository.save(e);
            updatePositions();
        });
        return entry;
    }

    public Optional<QueueEntry> skipCustomer(Long id) {
        Optional<QueueEntry> entry = queueRepository.findById(id);
        entry.ifPresent(e -> {
            e.setStatus(Status.SKIPPED);
            queueRepository.save(e);
            updatePositions();
        });
        return entry;
    }

    public Optional<QueueEntry> getCurrentlyServing() {
        return queueRepository.findFirstByStatusOrderByJoinedAtAsc(Status.SERVING);
    }

    public Map<String, Object> getQueueStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("waiting", queueRepository.countByStatus(Status.WAITING));
        stats.put("serving", queueRepository.countByStatus(Status.SERVING));
        stats.put("served", queueRepository.countByStatus(Status.SERVED));
        stats.put("skipped", queueRepository.countByStatus(Status.SKIPPED));
        stats.put("total", queueRepository.count());

        Double avgWait = queueRepository.findAverageWaitTime();
        stats.put("averageWaitMinutes", avgWait != null ? Math.round(avgWait) : 0);

        return stats;
    }

    public Map<String, Object> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<QueueEntry> allEntries = queueRepository.findAllByOrderByJoinedAtAsc();

        long totalServed = allEntries.stream().filter(e -> e.getStatus() == Status.SERVED).count();
        long totalSkipped = allEntries.stream().filter(e -> e.getStatus() == Status.SKIPPED).count();
        long totalWaiting = allEntries.stream().filter(e -> e.getStatus() == Status.WAITING).count();

        analytics.put("totalServed", totalServed);
        analytics.put("totalSkipped", totalSkipped);
        analytics.put("totalWaiting", totalWaiting);
        analytics.put("totalCustomers", allEntries.size());

        Double avgWait = queueRepository.findAverageWaitTime();
        analytics.put("averageWaitMinutes", avgWait != null ? Math.round(avgWait) : 0);

        // Service type breakdown
        Map<String, Long> serviceBreakdown = new HashMap<>();
        for (QueueEntry.ServiceType type : QueueEntry.ServiceType.values()) {
            long count = allEntries.stream()
                .filter(e -> e.getServiceType() == type)
                .count();
            serviceBreakdown.put(type.getDisplayName(), count);
        }
        analytics.put("serviceBreakdown", serviceBreakdown);

        // Recent served entries (last 10)
        List<QueueEntry> recentServed = allEntries.stream()
            .filter(e -> e.getStatus() == Status.SERVED || e.getStatus() == Status.SKIPPED)
            .sorted(Comparator.comparing(QueueEntry::getJoinedAt).reversed())
            .limit(10)
            .toList();
        analytics.put("recentEntries", recentServed);

        return analytics;
    }

    public void clearQueue() {
        List<QueueEntry> waiting = queueRepository.findByStatusOrderByJoinedAtAsc(Status.WAITING);
        waiting.forEach(e -> {
            e.setStatus(Status.SKIPPED);
            queueRepository.save(e);
        });
    }

    private void updatePositions() {
        List<QueueEntry> waiting = queueRepository.findByStatusOrderByJoinedAtAsc(Status.WAITING);
        for (int i = 0; i < waiting.size(); i++) {
            QueueEntry entry = waiting.get(i);
            entry.setPosition(i + 1);
            entry.setEstimatedWaitMinutes((i + 1) * MINUTES_PER_CUSTOMER);
            queueRepository.save(entry);
        }
    }

    private String generateTicketNumber(QueueEntry.ServiceType serviceType) {
        String prefix = switch (serviceType) {
            case GENERAL -> "G";
            case ACCOUNT -> "A";
            case LOAN -> "L";
            case SUPPORT -> "S";
        };
        return prefix + String.format("%03d", ticketCounter++);
    }
}

