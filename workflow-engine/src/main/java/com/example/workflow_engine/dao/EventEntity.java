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

    private String event;

    private String orderId;

    private LocalDateTime createdAt;

    public EventEntity() {}

    public EventEntity(String event, String orderId) {
        this.event = event;
        this.orderId = orderId;
        this.createdAt = LocalDateTime.now();
    }
}
