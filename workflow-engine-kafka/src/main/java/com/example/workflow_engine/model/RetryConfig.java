package com.example.workflow_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryConfig {
    private Integer maxAttempts;
    private Integer initialIntervalSeconds;
    private Integer maxIntervalSeconds;
    private Double backOff;
}
