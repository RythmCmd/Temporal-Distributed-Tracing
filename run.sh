#!/bin/bash

# ==========================================
# Temporal & New Relic Tracing Quickstart
# ==========================================

# 1. Provide your New Relic API Key
export MY_NEW_RELIC_API_KEY="NR-Ingest-API-KEY"
export NR_ENDPOINT="https://otlp.nr-data.net:4317"

if [ "$MY_NEW_RELIC_API_KEY" = "REPLACE_WITH_YOUR_NEW_RELIC_API_KEY" ]; then
    echo "ERROR: Please open run.sh and replace 'REPLACE_WITH_YOUR_NEW_RELIC_API_KEY' with your actual New Relic API key."
    exit 1
fi

echo "Checking prerequisites..."

# 1. Check if Temporal CLI is installed
if ! command -v temporal &> /dev/null; then
    echo "Temporal CLI is not installed."
    echo "Please install it using Homebrew: brew install temporal"
    echo "Or visit: https://docs.temporal.io/cli/#install"
    exit 1
fi

# 2. Start Temporal Developer Server if not running
if ! nc -z localhost 7233 &> /dev/null; then
    echo "Starting local Temporal development server in the background..."
    temporal server start-dev > temporal-server.log 2>&1 &
    TEMPORAL_SRV_PID=$!
    # Wait for the server to be ready
    sleep 5
    
    if ! nc -z localhost 7233 &> /dev/null; then
        echo "ERROR: Failed to start Temporal development server. See temporal-server.log"
        exit 1
    fi
    echo "Temporal server started (PID: $TEMPORAL_SRV_PID)."
else
    echo "Temporal development server is already running."
fi

# Attempt to find a Java 17+ JDK if available
if [ -d "/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" ]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
elif /usr/libexec/java_home -v 17 -F >/dev/null 2>&1; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 17 -F)
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
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
curl -X POST "http://localhost:8081/hello?id=123&type=test"

echo ""
echo "Workflow triggered successfully."
echo "The Activity simulates a 130-second long-running task."
echo "Heartbeat OpenTelemetry spans are being emitted every 60 seconds."
echo ""
echo "Tailing application logs. Press Ctrl+C to stop the application."
echo "Check your New Relic instance for the stitched traces!"
echo ""

# Tail logs and trap Ctrl+C to kill the app
if [ -n "$TEMPORAL_SRV_PID" ]; then
    trap "kill $APP_PID; kill $TEMPORAL_SRV_PID; exit" INT
else
    trap "kill $APP_PID; exit" INT
fi
tail -f app.log
