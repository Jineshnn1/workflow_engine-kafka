package com.example.workflow_engine.activities;

import com.example.workflow_engine.model.KafkaMessageSpec;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface KafkaActivities {

    void send(KafkaMessageSpec spec);
}
