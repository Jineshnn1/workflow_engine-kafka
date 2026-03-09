package com.example.workflow_engine.activities;

import com.example.workflow_engine.model.HttpRequestSpec;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HttpActivities {

    @ActivityMethod
    String callHttp(HttpRequestSpec spec);
}
