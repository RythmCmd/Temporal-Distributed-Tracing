# Temporal OTel Heartbeat Tracing Example

This repository contains a working, Temporal application designed to demonstrate how to emit OpenTelemetry Heartbeat Spans. 

This solves the issue where New Relic splits or drops distributed traces for Temporal workflows when a single Activity exceeds New Relic's 90-second span limit guardrail.

## How it works

1. **Temporal OpenTracing Bridge**: The application is configured with `io.temporal:temporal-opentracing` and `io.opentelemetry:opentelemetry-opentracing-shim` so the Temporal SDK auto-instruments workflows natively into your OpenTelemetry context.
2. **Scheduled Heartbeats**: Inside the `Activity001Impl.java`, the active OpenTelemetry `Context` is captured. A `ScheduledExecutorService` is fired off to emit an empty `heartbeat-span` every 60 seconds attached to that parent context.
3. **Trace Stitching**: Because New Relic continuously receives these small `heartbeat-span` updates during the 130-second void, the New Relic UI connects the single `runActivity` parent span without dropping the transaction!

## Quickstart Guide

### Prerequisites
* Java 17+ installed 
* A local instance of the [Temporal Developer Server](https://docs.temporal.io/cli/#start-dev-server) running (`temporal server start-dev`)

### Running the Example

1. Open `run.sh` in the root directory.
2. Replace `REPLACE_WITH_YOUR_NEW_RELIC_API_KEY` with your actual New Relic Ingest API Key.
3. Make the script executable and run it:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

The script will:
1. Compile the Spring Boot Temporal application.
2. Start the Temporal Workers and REST API on port `8080`.
3. Automatically trigger the `/hello` endpoint to kick off the long-running 130-second Temporal workflow.
4. Stream the Spring Boot logs to your terminal so you can watch the trace IDs and the 60-second heartbeats fire.

Once the workflow completes, check your New Relic Distributed Tracing UI. You will see a single connected trace containing the parent workflows, the 130-second activity, and two 60-second heartbeat children!
