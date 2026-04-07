package com.queue_manage.manage_queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queue_manage.manage_queue.model.QueueEntry;
import com.queue_manage.manage_queue.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class QueueApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QueueService queueService;

    @Autowired
    private ObjectMapper objectMapper;

    // =====================
    // Service Layer Tests
    // =====================

    @Test
    void contextLoads() {
        assertNotNull(queueService);
    }

    @Test
    void shouldJoinQueue() {
        QueueEntry entry = queueService.joinQueue("John Doe", QueueEntry.ServiceType.GENERAL);

        assertNotNull(entry);
        assertEquals("John Doe", entry.getCustomerName());
        assertEquals(QueueEntry.ServiceType.GENERAL, entry.getServiceType());
        assertEquals(QueueEntry.Status.WAITING, entry.getStatus());
        assertNotNull(entry.getTicketNumber());
        assertTrue(entry.getTicketNumber().startsWith("G"));
    }

    @Test
    void shouldAssignPositionsCorrectly() {
        queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);
        queueService.joinQueue("Bob", QueueEntry.ServiceType.ACCOUNT);
        QueueEntry third = queueService.joinQueue("Charlie", QueueEntry.ServiceType.LOAN);

        var queue = queueService.getWaitingQueue();
        assertEquals(3, queue.size());
        assertEquals(1, queue.get(0).getPosition());
        assertEquals(2, queue.get(1).getPosition());
        assertEquals(3, queue.get(2).getPosition());
    }

    @Test
    void shouldCallNextCustomer() {
        queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);
        queueService.joinQueue("Bob", QueueEntry.ServiceType.ACCOUNT);

        var next = queueService.callNext();
        assertTrue(next.isPresent());
        assertEquals("Alice", next.get().getCustomerName());
        assertEquals(QueueEntry.Status.SERVING, next.get().getStatus());
    }

    @Test
    void shouldServeCustomer() {
        QueueEntry entry = queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);
        queueService.callNext();

        var served = queueService.serveCustomer(entry.getId());
        assertTrue(served.isPresent());
        assertEquals(QueueEntry.Status.SERVED, served.get().getStatus());
        assertNotNull(served.get().getServedAt());
    }

    @Test
    void shouldSkipCustomer() {
        QueueEntry entry = queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);

        var skipped = queueService.skipCustomer(entry.getId());
        assertTrue(skipped.isPresent());
        assertEquals(QueueEntry.Status.SKIPPED, skipped.get().getStatus());
    }

    @Test
    void shouldReturnEmptyQueueAfterClear() {
        queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);
        queueService.joinQueue("Bob", QueueEntry.ServiceType.ACCOUNT);

        queueService.clearQueue();

        assertEquals(0, queueService.getWaitingQueue().size());
    }

    @Test
    void shouldGenerateCorrectTicketPrefixes() {
        QueueEntry g = queueService.joinQueue("A", QueueEntry.ServiceType.GENERAL);
        QueueEntry a = queueService.joinQueue("B", QueueEntry.ServiceType.ACCOUNT);
        QueueEntry l = queueService.joinQueue("C", QueueEntry.ServiceType.LOAN);
        QueueEntry s = queueService.joinQueue("D", QueueEntry.ServiceType.SUPPORT);

        assertTrue(g.getTicketNumber().startsWith("G"));
        assertTrue(a.getTicketNumber().startsWith("A"));
        assertTrue(l.getTicketNumber().startsWith("L"));
        assertTrue(s.getTicketNumber().startsWith("S"));
    }

    // =====================
    // REST Controller Tests
    // =====================

    @Test
    void shouldJoinQueueViaApi() throws Exception {
        Map<String, String> request = Map.of("customerName", "Jane Smith", "serviceType", "ACCOUNT");

        mockMvc.perform(post("/api/queue/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Jane Smith"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.ticketNumber").exists());
    }

    @Test
    void shouldReturnBadRequestWithoutName() throws Exception {
        Map<String, String> request = Map.of("serviceType", "GENERAL");

        mockMvc.perform(post("/api/queue/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetQueueStatus() throws Exception {
        queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);

        mockMvc.perform(get("/api/queue/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Alice"));
    }

    @Test
    void shouldGetEmptyQueueStatus() throws Exception {
        mockMvc.perform(get("/api/queue/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // @Test
    // void shouldCallNextViaApi() throws Exception {
    //     queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);

    //     mockMvc.perform(post("/api/queue/next"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.customerName").value("Alice"))
    //             .andExpect(jsonPath("$.status").value("SERVING"));
    // }

    @Test
    void shouldGetStatsViaApi() throws Exception {
        queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);
        queueService.joinQueue("Bob", QueueEntry.ServiceType.ACCOUNT);

        mockMvc.perform(get("/api/queue/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting").value(2));
    }

    @Test
    void shouldGetAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").exists())
                .andExpect(jsonPath("$.serviceBreakdown").exists());
    }

    // @Test
    // void shouldClearQueueViaApi() throws Exception {
    //     queueService.joinQueue("Alice", QueueEntry.ServiceType.GENERAL);

    //     mockMvc.perform(delete("/api/queue/clear"))
    //             .andExpect(status().isOk());

    //     assertEquals(0, queueService.getWaitingQueue().size());
    // }
}