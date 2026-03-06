package com.example.workflow_engine.workflow;

import com.example.workflow_engine.model.WorkflowDefinition;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Map;

@WorkflowInterface
public interface EngineWorkflow {

    @WorkflowMethod
    void run(WorkflowDefinition definition, Map<String, Object> initialState);

    @SignalMethod
    void externalEvent(Map<String, Object> payload);
}
