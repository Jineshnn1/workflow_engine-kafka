package com.example.workflow_engine.controller;

import com.example.workflow_engine.workflow.EngineWorkflow;
import org.springframework.web.bind.annotation.*;
import io.temporal.client.WorkflowClient;

import java.util.Map;

@RestController
@RequestMapping
public class CallbackController {

    private final WorkflowClient client;

    public CallbackController(WorkflowClient client) {
        this.client = client;
    }

    @PostMapping("/external-event/{workflowId}")
    public void onCallback(@PathVariable String workflowId,
                           @RequestBody Map<String, Object> payload) {

        EngineWorkflow workflow = client.newWorkflowStub(EngineWorkflow.class, workflowId);
        workflow.externalEvent(payload);
    }
}
