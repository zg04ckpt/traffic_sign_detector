# Traffic Sign Detection and Recognition System

## 1. Overview
The **Traffic Sign Detector** project is a distributed software system designed based on the Microservices Architecture. The core objective of the system is to provide a comprehensive platform for automating the management of datasets, configurations, and the training of Deep Learning models specialized in traffic sign recognition. The system serves research in Intelligent Transportation Systems (ITS) and autonomous vehicles.

## 2. System Architecture
The system is designed following a client-server model where the backend is decomposed into independent services, ensuring scalability, high availability, and maintainability.

### 2.1. Client-Side
- **`client3/`**: The Frontend platform developed on the **Vue.js** ecosystem combined with **Vite**. It provides an intuitive User Interface (UI) allowing users (researchers/data engineers) to interact with the system, manage training configurations, and monitor results.

### 2.2. Backend Microservices (`server5/`)
Developed on the **Java Spring Boot / Spring Cloud** platform, comprising the following component services:
- **`api-gateway/`**: The API Gateway, responsible for routing and access control from the client to internal services.
- **`discovery-server/`**: The Service Registry, supporting the service discovery mechanism for the distributed architecture.
- **`config-server/`**: The Centralized Configuration Server.
- **`dataset-service/`**: The service managing the lifecycle of datasets used for training.
- **`aimodel-service/`**: The service managing metadata and versions of AI models.
- **`training-orchestrator-service/`**: The service orchestrating the training process, managing event flows and synchronizing states between the backend and AI workers.

### 2.3. AI Processing Worker
- **`ai-training-service/`**: An independent module written in **Python**. It acts as a worker executing deep learning model training tasks (such as YOLO) based on input data. This worker interacts with the system through asynchronous mechanisms (RabbitMQ) and retrieves data directly from MinIO.

### 2.4. Core Infrastructure
- **RabbitMQ**: The message broker used for asynchronous communication between the `training-orchestrator-service` and the `ai-training-service`.
- **MinIO**: S3-compatible Object Storage responsible for persistently storing all unstructured data, including datasets (images, labels) and trained AI models (.pt files), ensuring the system remains stateless and horizontally scalable.

## 3. Technologies & Stack
- **Frontend**: Vue.js, TypeScript, Vite.
- **Backend**: Java 17+, Spring Boot, Spring Cloud (Gateway, Eureka, Config).
- **AI/Machine Learning**: Python, PyTorch/YOLO (or equivalent).
- **Infrastructure**: RabbitMQ (Message Queue), MinIO (Object Storage), PostgreSQL (Relational Database).
- **Deployment**: Dockerized services, supporting automated deployment on a **Kubernetes (K8s)** cluster (configurations located at `server5/k8s/`).

## 4. Installation and Operation Guide
*Note: Details for local installation and configuration for each microservice are described within the `README` file of their respective directories.*
1. System requirements: Docker, Kubernetes cluster (if deploying to production), Java JDK 17, Node.js, Python 3.x.
2. The build process can be executed via the `build-local-images.bat` script.
3. Deploy the services to Kubernetes using the scripts in the `k8s` directory.