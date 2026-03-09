package com.example.workflow_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {

    private String id;
    private Defaults defaults;
    private Map<String, String> variables;
    private List<Step> steps;
}
