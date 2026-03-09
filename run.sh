#!/bin/bash

# ==========================================
# Temporal & New Relic Tracing Quickstart
# ==========================================

# 1. Provide your New Relic API Key
export MY_NEW_RELIC_API_KEY="REPLACE_WITH_YOUR_NEW_RELIC_API_KEY"
export NR_ENDPOINT="https://otlp.nr-data.net:4317"

if [ "$MY_NEW_RELIC_API_KEY" = "REPLACE_WITH_YOUR_NEW_RELIC_API_KEY" ]; then
    echo "ERROR: Please open run.sh and replace 'REPLACE_WITH_YOUR_NEW_RELIC_API_KEY' with your actual New Relic API key."
    exit 1
fi

echo "Building the application..."
./mvnw clean package -DskipTests

# Ensure build was successful
if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed."
    exit 1
fi

echo "Starting the application..."
echo "Waiting for Spring Boot to initialize..."

# Start the Spring Boot application in the background
java -jar target/icm-tracing-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
APP_PID=$!

# Wait for application to be ready (approx 15 seconds)
sleep 15

echo "=========================================="
echo " Application is running! (PID: $APP_PID)"
echo " Triggering Temporal Workflow tracing..."
echo "=========================================="

# Trigger the workflow
curl -X POST "http://localhost:8080/hello?id=123&type=test"

echo ""
echo "Workflow triggered successfully."
echo "The Activity simulates a 130-second long-running task."
echo "Heartbeat OpenTelemetry spans are being emitted every 60 seconds."
echo ""
echo "Tailing application logs. Press Ctrl+C to stop the application."
echo "Check your New Relic instance for the stitched traces!"
echo ""

# Tail logs and trap Ctrl+C to kill the app
trap "kill $APP_PID; exit" INT
tail -f app.log
