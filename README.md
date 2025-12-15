# Workflow POC Application

This is a proof-of-concept (POC) application for a smart community workflow system. It demonstrates a distributed architecture using Spring Boot microservices or ASP.NET Core services, RabbitMQ for messaging, PostgreSQL for data persistence, and Docker Compose for container orchestration.

## Overview

The application consists of two main services that communicate asynchronously via RabbitMQ. There are two implementations available:

### Java Spring Boot Version
- **Workflow Manager**: Handles workflow creation, task management, and API endpoints for interacting with workflows.
- **Worker**: Processes tasks from a persistent RabbitMQ queue.
- **Data Persistence**: In-memory storage (for POC)

### .NET Core Version
- **Workflow Manager**: ASP.NET Core Web API for workflow management.
- **Worker**: .NET Core console application for task processing.
- **Data Persistence**: PostgreSQL database

## Architecture

### Java Version Services

The Java application is composed of the following services in Docker Compose:

- **RabbitMQ**: Message broker service using the official `rabbitmq:3-management` image. Provides AMQP messaging and a management UI.
  - Ports: 5672 (AMQP), 15672 (Management UI)
  - Default credentials: guest/guest
  - Persistent data volume: `rabbitmq_data`

- **Workflow-Manager**: Spring Boot application for workflow management.
  - Built with Java 21 LTS
  - Exposes REST API on port 8080
  - Includes OpenAPI/Swagger documentation
  - Depends on RabbitMQ service

- **Worker**: Spring Boot application for task processing.
  - Built with Java 21 LTS
  - Consumes tasks from RabbitMQ queue
  - Processes and completes tasks asynchronously

### .NET Core Version Services

The .NET application is composed of the following services in Docker Compose:

- **PostgreSQL**: Database service using PostgreSQL 15 image.
  - Port: 5432
  - Database: workflowdb
  - Default credentials: postgres/postgres
  - Persistent data volume: `postgres_data`

- **RabbitMQ**: Message broker service using the official `rabbitmq:3-management` image. Provides AMQP messaging and a management UI.
  - Ports: 5672 (AMQP), 15672 (Management UI)
  - Default credentials: guest/guest
  - Persistent data volume: `rabbitmq_data`

- **Workflow-Manager-Dotnet**: ASP.NET Core Web API for workflow management.
  - Built with .NET 8.0
  - Exposes REST API on port 8080
  - Includes OpenAPI/Swagger documentation
  - Depends on PostgreSQL and RabbitMQ services

- **Worker-Dotnet**: .NET Core console application for task processing.
  - Built with .NET 8.0
  - Consumes tasks from RabbitMQ queue
  - Processes and completes tasks asynchronously
  - Depends on PostgreSQL and RabbitMQ services

### Java Projects

#### Workflow-Manager (`workflow-manager/`)
- **Main Class**: `WorkflowPocApplication.java`
- **Key Components**:
  - `WorkflowController`: REST API endpoints
  - `WorkflowService`: Business logic for workflows
  - `TaskStoreService`: Task persistence
  - `MessagingService`: RabbitMQ integration
  - `TaskCompleteProcessor`: Task completion handling
- **Dependencies**: Spring Boot Web, AMQP, Data JPA, SpringDoc OpenAPI

#### Worker (`worker/`)
- **Main Class**: `WorkerPocApplication.java`
- **Key Components**:
  - `WorkerService`: Task processing logic
  - `TaskStoreService`: Task persistence
- **Dependencies**: Spring Boot AMQP, Data JPA

### .NET Core Projects

The application also includes .NET Core versions of the services for comparison and cross-platform development.

#### Workflow-Manager-Dotnet (`workflow-manager-dotnet/`)
- **Main Class**: `Program.cs` (ASP.NET Core Web API)
- **Key Components**:
  - `WorkflowController`: REST API endpoints
  - `WorkflowService`: Business logic for workflows
  - `TaskStoreService`: Task persistence (PostgreSQL)
  - `RabbitMQService`: RabbitMQ integration
  - `ITaskCompleteProcessor`: Task completion handling interface
  - `ApplicationDbContext`: Entity Framework Core DbContext
- **Dependencies**: ASP.NET Core, RabbitMQ.Client, Swashbuckle (OpenAPI), Npgsql.EntityFrameworkCore.PostgreSQL

#### Worker-Dotnet (`worker-dotnet/`)
- **Main Class**: `Program.cs` (Console application with Hosted Service)
- **Key Components**:
  - `WorkerService`: Task processing logic
  - `TaskStoreService`: Task persistence (PostgreSQL)
  - `WorkerBackgroundService`: Background service for message listening
  - `ApplicationDbContext`: Entity Framework Core DbContext
- **Dependencies**: .NET Core, RabbitMQ.Client, Microsoft.Extensions.Hosting, Npgsql.EntityFrameworkCore.PostgreSQL

## Prerequisites

- Docker
- Docker Compose (or Podman Compose)

## Building and Running

### Java Version

#### Build the Application

1. Clone or navigate to the project directory:
   ```bash
   cd /path/to/workflow-poc
   ```

2. Build the Docker images:
   ```bash
   docker-compose build
   ```
   or
   ```bash
   podman compose build
   ```

   This will:
   - Build the Maven projects inside Docker containers
   - Create JAR files for both services
   - Package them into runtime images

#### Run the Application

1. Start all services:
   ```bash
   docker-compose up
   ```
   or
   ```bash
   podman compose up
   ```

2. The services will start in the following order:
   - RabbitMQ (with health check)
   - Workflow-Manager (after RabbitMQ is healthy)
   - Worker (after RabbitMQ is healthy)

### .NET Core Version

#### Build the Application

1. Build the Docker images:
   ```bash
   docker-compose -f docker-compose-dotnet.yml build
   ```

   This will:
   - Build the .NET projects inside Docker containers
   - Create assemblies for both services
   - Package them into runtime images

#### Run the Application

1. Start all services:
   ```bash
   docker-compose -f docker-compose-dotnet.yml up
   ```

2. The services will start in the following order:
   - RabbitMQ (with health check)
   - Workflow-Manager-Dotnet (after RabbitMQ is healthy)
   - Worker-Dotnet (after RabbitMQ is healthy)


3. Test the execution submitting a simple workflow (e.g., using curl or via OpenAPI Explorer): 
```
curl -X POST http://localhost:8080/api/workflow -H "Content-Type: application/json" -d '{ "name": "wf", "id": "1","tasks": [{ "type": "t1" }, { "type": "t2" }]}'
```

### Access the Application

#### Java Version
- **Workflow Manager API**: http://localhost:8080
- **Swagger/OpenAPI Documentation**: http://localhost:8080/swagger-ui/index.html
- **RabbitMQ Management UI**: http://localhost:15672 (username: guest, password: guest)

#### .NET Core Version
- **Workflow Manager API**: http://localhost:8080
- **Swagger/OpenAPI Documentation**: http://localhost:8080/swagger
- **RabbitMQ Management UI**: http://localhost:15672 (username: guest, password: guest)

### Stopping the Application

#### Java Version
```bash
docker-compose down
```
or
```bash
podman compose down
```

#### .NET Core Version
```bash
docker-compose -f docker-compose-dotnet.yml down
```

To remove volumes (including RabbitMQ data):
```bash
docker-compose down -v
```
or
```bash
docker-compose -f docker-compose-dotnet.yml down -v
```

## Configuration

### Java Version
The application uses environment variables for configuration (defined in `application.yml`):

- `SERVER_PORT`: Application port (default: 8080)
- `RABBITMQ_HOST`: RabbitMQ host (default: localhost, set to `rabbitmq` in Docker)
- `RABBITMQ_PORT`: RabbitMQ port (default: 5672)
- `RABBITMQ_USERNAME`: RabbitMQ username (default: guest)
- `RABBITMQ_PASSWORD`: RabbitMQ password (default: guest)

### .NET Core Version
Configuration is defined in `appsettings.json` and can be overridden with environment variables:

- `ConnectionStrings__DefaultConnection`: PostgreSQL connection string (default: Host=postgres;Port=5432;Database=workflowdb;Username=postgres;Password=postgres)
- `RabbitMQ__Host`: RabbitMQ host (default: localhost, set to `rabbitmq` in Docker)
- `RabbitMQ__Port`: RabbitMQ port (default: 5672)
- `RabbitMQ__Username`: RabbitMQ username (default: guest)
- `RabbitMQ__Password`: RabbitMQ password (default: guest)
- `ASPNETCORE_URLS`: URLs for ASP.NET Core (default: http://+:8080 for workflow-manager-dotnet)

## Development

### Local Development

#### Java Version
To run services locally (outside Docker):

1. Ensure Java 21 and Maven are installed
2. Start RabbitMQ locally or via Docker
3. Build and run each service:
   ```bash
   cd workflow-manager
   mvn clean package
   java -jar target/*.jar

   cd ../worker
   mvn clean package
   java -jar target/*.jar
   ```

#### .NET Core Version
To run services locally (outside Docker):

1. Ensure .NET 8.0 SDK is installed
2. Start RabbitMQ locally or via Docker
3. Build and run each service:
   ```bash
   cd workflow-manager-dotnet
   dotnet run

   cd ../worker-dotnet
   dotnet run
   ```

### Testing

#### Java Version
Run tests for each service:
```bash
cd workflow-manager
mvn test

cd ../worker
mvn test
```

#### .NET Core Version
Run tests for each service:
```bash
cd workflow-manager-dotnet
dotnet test

cd ../worker-dotnet
dotnet test
```

## Technologies Used

### Java Version
- **Java 21 LTS**: Runtime environment
- **Spring Boot**: Framework for microservices
- **RabbitMQ**: Message broker
- **Maven**: Build tool
- **Docker & Docker Compose**: Containerization and orchestration
- **SpringDoc OpenAPI**: API documentation

### .NET Core Version
- **.NET 8.0**: Runtime environment
- **ASP.NET Core**: Framework for web APIs
- **Entity Framework Core**: ORM for data access
- **PostgreSQL**: Database for persistence
- **RabbitMQ.Client**: RabbitMQ .NET client
- **Docker & Docker Compose**: Containerization and orchestration
- **Swashbuckle**: OpenAPI documentation</content>
<parameter name="filePath">/Users/raman/Documents/Work/SO/projects/SmartCommunity/DIPS/workflow-poc/README.md