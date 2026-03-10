package click.yinsb.icmtracing.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OtelConfig {
        @Bean
        OpenTelemetry openTelemetry() {
                Resource resource = Resource.getDefault()
                                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"),
                                                "multi-span-dt")));

                String endpoint = System.getenv("NR_ENDPOINT");
                if (endpoint == null || endpoint.isEmpty()) {
                        endpoint = "http://localhost:4317";
                }
                String apiKey = System.getenv("MY_NEW_RELIC_API_KEY");

                System.out.println("Configuring OpenTelemetry with endpoint: " + endpoint);
                System.out.println("Using New Relic API Key: (length: " + (apiKey != null ? apiKey.length() : "null")
                                + ")");

                var exporterBuilder = OtlpGrpcSpanExporter.builder()
                                .setEndpoint(endpoint);
                if (apiKey != null && !apiKey.isEmpty()) {
                        exporterBuilder.addHeader("api-key", apiKey);
                }

                SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                                .setResource(resource)
                                .addSpanProcessor(BatchSpanProcessor.builder(exporterBuilder.build())
                                                .build())
                                .build();

                return OpenTelemetrySdk.builder()
                                .setTracerProvider(tracerProvider)
                                .setPropagators(ContextPropagators.create(
                                                TextMapPropagator.composite(
                                                                W3CTraceContextPropagator.getInstance(),
                                                                W3CBaggagePropagator.getInstance())))
                                .build();
        }

        @Bean
        io.opentracing.Tracer openTracingTracer(OpenTelemetry openTelemetry) {
                return io.opentelemetry.opentracingshim.OpenTracingShim.createTracerShim(openTelemetry);
        }

}
