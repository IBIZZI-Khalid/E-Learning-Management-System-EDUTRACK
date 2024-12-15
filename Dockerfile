# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Install Maven and necessary system dependencies
RUN apt-get update && \
    apt-get install -y maven \
    libxext6 libxrender1 libxtst6 libxi6 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy the pom.xml file
COPY pom.xml .

# Download dependencies (this can help with caching)
RUN mvn dependency:go-offline

# Copy the entire project
COPY . .

# Build the application
RUN mvn clean package

# Expose any necessary ports (if your app uses any)
EXPOSE 8080

# Command to run the JavaFX application
# Replace 'com.example.MainApp' with your actual main class
CMD ["mvn", "javafx:run", "-Djavafx.mainClass=com.edutrack.team.MainApp"]