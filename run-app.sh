#!/bin/bash

# Build the application
cd app && mvn clean package -DskipTests && cd ..

# Start the jar with OpenTelemetry agent
OTEL_AGENT="opentelemetry-javaagent.jar"
java -javaagent:$OTEL_AGENT \
    -jar app/target/wishlist-1.0.0.jar
