package com.example.workflow_engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaMessageSpec {
    private String topic;
    private String key;
    private String valueJson;
}
