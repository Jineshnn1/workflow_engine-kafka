package com.example.workflow_engine.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {

    List<EventEntity> findByProcessIdOrderByCreatedAtAsc(String processId);

}
