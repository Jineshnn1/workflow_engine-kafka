package com.example.workflow_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Step {
    private String id;
    private String type;
    private String name;
    private String activity;
    private Map<String, Object> input;
    private String saveAs;

    // decision‑related
    private String source;
    private Map<String, String> cases;
    private String defaultNext;
}
