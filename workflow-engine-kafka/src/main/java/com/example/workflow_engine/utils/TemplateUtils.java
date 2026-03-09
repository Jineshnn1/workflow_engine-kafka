package com.example.workflow_engine.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemplateUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String renderObjectToJson(Object template, Map<String, Object> state) {
        if (template == null) return null;
        if (template instanceof String s) {
            return renderString(s, state);
        }

        try {
            String writeValueAsString = MAPPER.writeValueAsString(template);
            return writeValueAsString;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> resolveVariables(Map<String, String> vars) {
        Map<String, Object> result = new HashMap<>();
        if (vars == null) return result;

        for (Map.Entry<String, String> e : vars.entrySet()) {
            String v = e.getValue();
            if (v != null && v.contains("{{uuid}}")) {
                v = v.replace("{{uuid}}", UUID.randomUUID().toString());
            }
            result.put(e.getKey(), v);
        }
        return result;
    }

    // Replace {{state.key}} with state.get("key").toString()
    public static String renderString(String template, Map<String, Object> state) {
        if (template == null) return null;
        String result = template;
        for (Map.Entry<String, Object> entry : state.entrySet()) {
            String placeholder = "{{state." + entry.getKey() + "}}";
            if (entry.getValue() != null) {
                result = result.replace(placeholder, entry.getValue().toString());
            }
        }
        return result;
    }
}
