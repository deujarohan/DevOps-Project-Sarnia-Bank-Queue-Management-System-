package com.queue_manage.manage_queue.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entries")
public class QueueEntry {

    public enum Status {
        WAITING, SERVING, SERVED, SKIPPED
    }

    public enum ServiceType {
        GENERAL("General Inquiry"),
        ACCOUNT("Account Services"),
        LOAN("Loan & Mortgage"),
        SUPPORT("Technical Support");

        private final String displayName;
        ServiceType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String customerName;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String ticketNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime joinedAt;
    private LocalDateTime servedAt;
    private Integer position;
    private Integer estimatedWaitMinutes;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
        this.status = Status.WAITING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getServedAt() { return servedAt; }
    public void setServedAt(LocalDateTime servedAt) { this.servedAt = servedAt; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
}
