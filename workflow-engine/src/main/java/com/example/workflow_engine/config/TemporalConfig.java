package com.example.workflow_engine.config;

import com.example.workflow_engine.activities.HttpActivitiesImpl;
import com.example.workflow_engine.activities.KafkaActivitiesImpl;
import com.example.workflow_engine.workflow.EngineWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.util.Properties;

@Component
public class TemporalConfig implements DisposableBean {
    public static final String TASK_QUEUE = "DSL_TASK_QUEUE";

    private WorkerFactory workerFactory;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        // Point to your Temporal cluster; local dev uses in‑proc by default
        return WorkflowServiceStubs.newLocalServiceStubs();
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs stubs) {
        return WorkflowClient.newInstance(stubs);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client, HttpClient httpClient,
                                       KafkaProducer<String, String> kafkaProducer) {
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(EngineWorkflowImpl.class);
        worker.registerActivitiesImplementations(new HttpActivitiesImpl(), new KafkaActivitiesImpl(kafkaProducer));

        factory.start();
        this.workerFactory = factory;
        return factory;
    }

    @Override
    public void destroy() {
        if (workerFactory != null) {
            workerFactory.shutdown();
        }
    }
}
