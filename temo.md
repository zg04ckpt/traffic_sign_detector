```mermaid
graph TD
  subgraph Infra
    MQ[(RabbitMQ)]
    Storage[(Shared Storage / Filesystem)]
  end

  APIGW[api-gateway]
  CONFIG[config-server]
  DISC[discovery-server]

  DATASET[dataset-service]
  AIMODEL[aimodel-service]
  ORCH[training-orchestrator-service]
  WORKER[training-worker-service]

  %% REST dependencies
  ORCH -->|REST /api/mo-hinh/{id}/phien-ban| AIMODEL

  %% Messaging dependencies
  ORCH -->|publish TrainingJobMessage| MQ
  WORKER -->|publish TrainingStatusMessage| MQ
  MQ -->|consume TrainingJobMessage| WORKER
  MQ -->|consume TrainingStatusMessage| ORCH

  %% File-based dependencies
  WORKER -->|reads uploads| Storage
  DATASET -->|writes uploads| Storage
  WORKER -->|writes model outputs| Storage
  ORCH -->|reads temp model artifacts| Storage
  AIMODEL -->|stores model versions| Storage

  %% Gateway routing (logical)
  APIGW --> DATASET
  APIGW --> AIMODEL
  APIGW --> ORCH
```