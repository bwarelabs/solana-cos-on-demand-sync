#!/bin/bash
set -e  # Exit immediately if any command fails

echo "Starting Solana Syncer API..."

# Navigate to the app directory
cd /app

# Find the JAR file
JAR_FILE=$(find target -type f -name "*.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "Error: No JAR file found in /app/target. Build might have failed."
  exit 1
fi

# Run the Spring Boot application
java -jar "$JAR_FILE"
