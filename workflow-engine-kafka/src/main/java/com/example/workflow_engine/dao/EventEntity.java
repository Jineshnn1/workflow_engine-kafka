package com.example.workflow_engine.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String processId;
    private String event;
    private String paymentId;
    private LocalDateTime createdAt;

    public EventEntity() {}

    public EventEntity(String processId, String event, String paymentId) {
        this.processId = processId;
        this.event = event;
        this.paymentId = paymentId;
        this.createdAt = LocalDateTime.now();
    }
}
