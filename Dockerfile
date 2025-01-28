FROM eclipse-temurin:22

# Install necessary dependencies
RUN apt-get update && apt-get install -y maven vim curl && rm -rf /var/lib/apt/lists/*

# Set up application workspace
WORKDIR /app

# Copy project files
COPY . /app

# Build the application
RUN mvn clean package -DskipTests

# Define the entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Run the entrypoint script
ENTRYPOINT ["/entrypoint.sh"]
