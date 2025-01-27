FROM eclipse-temurin:22

# Install necessary dependencies
RUN apt-get update && apt-get install -y maven vim

# Set up application workspace
WORKDIR /app

# Copy Maven dependencies for faster incremental builds
COPY pom.xml pom.xml
RUN mvn dependency:resolve

CMD ["/bin/bash"]
