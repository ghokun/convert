#!/bin/bash

# Exit on error
set -e

# Compile the project and build a native executable
./gradlew :cli:nativeRun
# Run the native executable
./cli/build/native/nativeCompile/convert
# Run the application with the agent on JVM
./gradlew -Pagent :cli:run
# Copy metadata into /META-INF/native-image directory
./gradlew :cli:metadataCopy --task run --dir cli/src/main/resources/META-INF/native-image
# Build a native executable using metadata
./gradlew :cli:nativeCompile
# Run the native executable
./cli/build/native/nativeCompile/convert
# Run JUnit tests
./gradlew :cli:nativeTest
# Run tests on JVM with the agent
./gradlew -Pagent test
# Test building a native executable using metadata
./gradlew -Pagent :cli:nativeTest
