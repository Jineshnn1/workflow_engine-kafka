package com.example.workflow_engine.controller;

import com.example.workflow_engine.config.TemporalConfig;
import com.example.workflow_engine.model.WorkflowDefinition;
import com.example.workflow_engine.workflow.EngineWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/workflows")
public class WorkflowEngineController {
    private final WorkflowClient client;

    public WorkflowEngineController(WorkflowClient client) {
        this.client = client;
    }

    @PostMapping("/start")
    public String start(@RequestBody WorkflowDefinition def) {
        String workflowId = def.getId() + "-" + System.currentTimeMillis();

        EngineWorkflow wf = client.newWorkflowStub(
                EngineWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalConfig.TASK_QUEUE)
                        .setWorkflowId(workflowId)
                        .build()
        );

        // initial state is empty; variables from JSON will be resolved inside workflow
        WorkflowClient.start(wf::run, def, new HashMap<String, Object>(), workflowId);

        return workflowId;
    }
}
