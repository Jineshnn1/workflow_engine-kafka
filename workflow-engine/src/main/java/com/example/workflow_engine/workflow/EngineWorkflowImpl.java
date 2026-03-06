package com.example.workflow_engine.workflow;

import com.example.workflow_engine.activities.HttpActivities;
import com.example.workflow_engine.activities.KafkaActivities;
import com.example.workflow_engine.model.*;
import com.example.workflow_engine.utils.TemplateUtils;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EngineWorkflowImpl implements EngineWorkflow {

    private final List<Map<String, Object>> externalEvents = new ArrayList<>();

    @Override
    public void run(WorkflowDefinition definition, Map<String, Object> initialState) {
        Map<String, Object> state =
                initialState != null ? new HashMap<>(initialState) : new HashMap<>();

        // 1) Resolve variables and put into state
        Map<String, Object> resolvedVars = TemplateUtils.resolveVariables(definition.getVariables());
        state.putAll(resolvedVars);

        Map<String, Step> stepsById = definition.getSteps().stream()
                .collect(Collectors.toMap(Step::getId, s -> s));

        String currentId = definition.getSteps().isEmpty()
                ? null
                : definition.getSteps().get(0).getId();

        // 2) Execute steps sequentially

//        for (Step step : definition.getSteps()) {
//            String type = step.getType();
//            if ("activity".equalsIgnoreCase(type)) {
//                if ("http".equalsIgnoreCase(step.getActivity())) {
//                    runHttpActivityStep(definition.getDefaults(), step, state);
//                    log.info("Executing step {} and type {}", step.getId(), step.getType());
//                } else {
//                    throw new IllegalStateException("Unsupported activity: " + step.getActivity());
//                }
//            } else if ("signal".equalsIgnoreCase(type)) {
//                runSignalStep(step, state);
//            } else {
//                throw new IllegalStateException("Unsupported step type: " + type);
//            }
//        }

        while (currentId != null) {
            Step step = stepsById.get(currentId);
            if (step == null) {
                throw new IllegalStateException("Unknown step id: " + currentId);
            }

            switch (step.getType().toLowerCase()) {
                case "activity":
                    if ("http".equalsIgnoreCase(step.getActivity())) {
                        runHttpActivityStep(definition.getDefaults(), step, state);
                    }
                    else if ("kafka".equalsIgnoreCase(step.getActivity())) {
                        runKafkaActivityStep(step, state);
                    }
                    else {
                        throw new IllegalStateException("Unsupported activity: " + step.getActivity());
                    }
                    // for activities, go to next by explicit "next" or just next in list
                    currentId = findNextId(definition, currentId, step);
                    break;

                case "signal":
                    runSignalStep(step, state);
                    currentId = findNextId(definition, currentId, step);
                    break;

                case "decision":
                    currentId = runDecisionStep(step, state);
                    break;

                default:
                    throw new IllegalStateException("Unsupported step type: " + step.getType());
            }
        }

    }

    private void runKafkaActivityStep(Step step, Map<String, Object> state) {
        Map<String, Object> input = step.getInput();
        if (input == null) {
            throw new IllegalStateException("kafka activity requires 'input'");
        }

        String topic = input.get("topic").toString();
        String keyTemplate = input.get("key") != null ? input.get("key").toString() : null;
        Object valueTemplate = input.get("value");

        String key = TemplateUtils.renderString(keyTemplate, state);
        String valueJson = TemplateUtils.renderObjectToJson(valueTemplate, state);

        KafkaMessageSpec spec = new KafkaMessageSpec();
        spec.setTopic(topic);
        spec.setKey(key);
        spec.setValueJson(valueJson);

        KafkaActivities kafkaActivities = Workflow.newActivityStub(
                KafkaActivities.class,
                ActivityOptions.newBuilder()
                        .setStartToCloseTimeout(Duration.ofSeconds(10))
                        .build()
        );

        kafkaActivities.send(spec);
    }

    private String runDecisionStep(Step step, Map<String, Object> state) {
        String sourcePath = step.getSource(); // e.g. "approval.status"
        if (sourcePath == null) {
            throw new IllegalStateException("decision step must have 'source'");
        }
        Object value = resolvePath(state, sourcePath);

        String stringValue = value != null ? value.toString() : null;

        if (stringValue != null && step.getCases() != null) {
            String next = step.getCases().get(stringValue);
            if (next != null) {
                return next;
            }
        }

        return step.getDefaultNext();
    }

    @SuppressWarnings("unchecked")
    private Object resolvePath(Map<String, Object> state, String path) {
        String[] parts = path.split("\\.");
        Object current = state;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }


    private String findNextId(WorkflowDefinition definition, String currentId, Step step) {
        List<Step> steps = definition.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getId().equals(currentId)) {
                return (i + 1 < steps.size()) ? steps.get(i + 1).getId() : null;
            }
        }
        return null;
    }

    private void runSignalStep(Step step, Map<String, Object> state) {
        Map<String, Object> payload = waitForExternalEvent();
        if (step.getSaveAs() != null) {
            state.put(step.getSaveAs(), payload);
        }
    }

    private Map<String, Object> waitForExternalEvent() {
        Workflow.await(() -> !externalEvents.isEmpty());
        return externalEvents.remove(0);
    }

    @Override
    public void externalEvent(Map<String, Object> payload) {
        externalEvents.add(payload);
    }

    private void runHttpActivityStep(Defaults defaults, Step step, Map<String, Object> state) {
        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(10))
                .setRetryOptions(buildRetryOptions(defaults != null ? defaults.getRetry() : null))
                .build();

        HttpActivities activities = Workflow.newActivityStub(HttpActivities.class, options);

        Map<String, Object> input = step.getInput();
        String method = input != null && input.get("method") != null
                ? input.get("method").toString()
                : "GET";

        String urlTemplate = input != null && input.get("url") != null
                ? input.get("url").toString()
                : null;

        String url = TemplateUtils.renderString(urlTemplate, state);

        HttpRequestSpec spec = new HttpRequestSpec();
        spec.setMethod(method);
        spec.setUrl(url);

        String responseBody = activities.callHttp(spec);

        if (step.getSaveAs() != null) {
            state.put(step.getSaveAs(), responseBody);
        }
    }

    private RetryOptions buildRetryOptions(RetryConfig rc) {
        RetryOptions.Builder rb = RetryOptions.newBuilder();

        if (rc != null) {
            if (rc.getMaxAttempts() != null) {
                rb.setMaximumAttempts(rc.getMaxAttempts());
            }
            if (rc.getInitialIntervalSeconds() != null) {
                rb.setInitialInterval(Duration.ofSeconds(rc.getInitialIntervalSeconds()));
            }
            if (rc.getMaxIntervalSeconds() != null) {
                rb.setMaximumInterval(Duration.ofSeconds(rc.getMaxIntervalSeconds()));
            }
            if (rc.getBackOff() != null) {
                rb.setBackoffCoefficient(rc.getBackOff());
            }
        }

        return rb.build();
    }
}
