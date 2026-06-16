# Aimodel Version Storage

This directory stores finalized model version artifacts managed by `aimodel-service`.

Expected layout:
- `mo-hinh-<modelId>/...pt`

Flow:
1. `ai-training-service` writes temporary artifacts to `ai-training-service/runtime/training/outputs` (or `/data/runtime/training/outputs` on Kubernetes).
2. On "save version", `training-orchestrator-service` copies artifact here.
3. The temporary artifact is removed after successful version creation in `aimodel-service`.
