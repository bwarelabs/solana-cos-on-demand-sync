FROM eclipse-temurin:22

# Install necessary dependencies
RUN apt-get update && apt-get install -y maven vim curl && rm -rf /var/lib/apt/lists/*

# Set up application workspace
WORKDIR /app

