package click.yinsb.icmtracing.temporal.activities;

import click.yinsb.icmtracing.temporal.model.Constants;
import click.yinsb.icmtracing.temporal.model.EventMessage;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ActivityImpl(taskQueues = { Constants.ICM_TASK_QUEUE })
public class Activity001Impl implements Activity001 {
    private static final Logger log = LoggerFactory.getLogger(Activity001Impl.class);

    private final RestTemplate restTemplate;
    private final Tracer tracer;

    public Activity001Impl(RestTemplate restTemplate, OpenTelemetry openTelemetry) {
        this.restTemplate = restTemplate;
        this.tracer = openTelemetry.getTracer(Activity001Impl.class.getName());
    }

    @Override
    public void runActivity(EventMessage eventMessage) {
        log.info("runActivity called");

        Context currentContext = Context.current();
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            Span heartbeatSpan = tracer.spanBuilder("heartbeat-span")
                    .setParent(currentContext)
                    .startSpan();
            log.info("Emitted OTel Heartbeat Span");
            heartbeatSpan.end();
        }, 60, 60, TimeUnit.SECONDS);

        try {
            log.info("Simulating long running operation for 130 seconds...");
            Thread.sleep(130000); // Wait 130 seconds to exceed the 90-second threshold
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Long running operation interrupted", e);
        } catch (Exception e) {
            log.error("Error during long running operation", e);
        } finally {
            executor.shutdownNow();
            log.info("runActivity completed, heartbeat executor shut down");
        }
    }

}
