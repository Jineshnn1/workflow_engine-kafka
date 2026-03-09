package com.example.workflow_engine.controller;

import com.example.workflow_engine.dao.EventEntity;
import com.example.workflow_engine.dao.EventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workflow-events")
public class WorkflowEventController {
    private final EventRepository repository;

    public WorkflowEventController(EventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{processId}")
    public List<EventEntity> getEvents(@PathVariable String processId) {
        return repository.findByProcessIdOrderByCreatedAtAsc(processId);
    }
}
