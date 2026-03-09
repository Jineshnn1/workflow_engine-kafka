package com.example.workflow_engine.activities;

import com.example.workflow_engine.model.HttpRequestSpec;

public class HttpActivitiesImpl implements HttpActivities{
    @Override
    public String callHttp(HttpRequestSpec spec) {
        return "test";
    }
}
