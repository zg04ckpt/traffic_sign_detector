import { reactive } from 'vue'
import type { Mau, MoHinh, PhienBan, ThongTinHL } from '@/model'

const STORAGE_KEY = 'client3.training.selection.v1'

export interface TrainingConfig {
  batchSize: number
  epochs: number
  learningRate: number
  imageSize: number
  earlyStoppingPatience: number
  deviceType: string
  optimizer: string
}

export interface TrainingStatusPayload {
  state?: string
  detail?: string
  updatedAt?: string
  currentEpoch?: number
  precision?: number
  recall?: number
  logLine?: string
}

export interface TrainingStatusEvent {
  state?: string
  detail?: string
  updatedAt?: string
  currentEpoch?: number
  precision?: number
  recall?: number
  logLine?: string
}

export interface SelectedSampleCacheItem {
  sampleId: number | null
  sampleName: string
  datasetId: number | null
  datasetName: string
}

interface PersistedTrainingSelection {
  selectedModel: MoHinh | null
  selectedVersion: PhienBan | null
  selectedDatasetId: number | null
  selectedSamples: Mau[]
  selectedSampleCache: SelectedSampleCacheItem[]
  config: TrainingConfig
}

function defaultConfig(): TrainingConfig {
  return {
    batchSize: 256,
    epochs: 2,
    learningRate: 0.01,
    imageSize: 416,
    earlyStoppingPatience: 5,
    deviceType: 'cpu',
    optimizer: 'Adam',
  }
}

function loadPersistedSelection(): PersistedTrainingSelection {
  const fallback: PersistedTrainingSelection = {
    selectedModel: null,
    selectedVersion: null,
    selectedDatasetId: null,
    selectedSamples: [],
    selectedSampleCache: [],
    config: defaultConfig(),
  }

  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return fallback
    }

    const parsed = JSON.parse(raw) as Partial<PersistedTrainingSelection>
    const config = parsed.config ?? fallback.config

    const selectedSampleCache = Array.isArray(parsed.selectedSampleCache)
      ? parsed.selectedSampleCache.map(item => ({
        sampleId: typeof item?.sampleId === 'number' ? item.sampleId : null,
        sampleName: String(item?.sampleName ?? ''),
        datasetId: typeof item?.datasetId === 'number' ? item.datasetId : null,
        datasetName: String(item?.datasetName ?? 'Không xác định'),
      }))
      : []

    return {
      selectedModel: parsed.selectedModel ?? null,
      selectedVersion: parsed.selectedVersion ?? null,
      selectedDatasetId: typeof parsed.selectedDatasetId === 'number' ? parsed.selectedDatasetId : null,
      selectedSamples: Array.isArray(parsed.selectedSamples) ? parsed.selectedSamples : [],
      selectedSampleCache,
      config: {
        batchSize: Number(config.batchSize) > 0 ? Number(config.batchSize) : fallback.config.batchSize,
        epochs: Number(config.epochs) > 0 ? Number(config.epochs) : fallback.config.epochs,
        learningRate: Number(config.learningRate) > 0 ? Number(config.learningRate) : fallback.config.learningRate,
        imageSize: Number(config.imageSize) > 0 ? Number(config.imageSize) : fallback.config.imageSize,
        earlyStoppingPatience: Number(config.earlyStoppingPatience) >= 0
          ? Number(config.earlyStoppingPatience)
          : fallback.config.earlyStoppingPatience,
        deviceType: String(config.deviceType || fallback.config.deviceType),
        optimizer: String(config.optimizer || fallback.config.optimizer),
      },
    }
  } catch {
    return fallback
  }
}

const persisted = loadPersistedSelection()

export const trainingState = reactive({
  selectedModel: persisted.selectedModel,
  selectedVersion: persisted.selectedVersion,
  selectedDatasetId: persisted.selectedDatasetId as number | null,
  selectedSamples: persisted.selectedSamples,
  selectedSampleCache: persisted.selectedSampleCache as SelectedSampleCacheItem[],
  config: persisted.config,
  trainingId: null as number | null,
  trackingId: '',
  latestResult: null as ThongTinHL | null,
  latestStatus: null as TrainingStatusPayload | null,
  timeline: [] as TrainingStatusEvent[],
})

export function persistTrainingSelection(): void {
  const data: PersistedTrainingSelection = {
    selectedModel: trainingState.selectedModel,
    selectedVersion: trainingState.selectedVersion,
    selectedDatasetId: trainingState.selectedDatasetId,
    selectedSamples: trainingState.selectedSamples,
    selectedSampleCache: trainingState.selectedSampleCache,
    config: trainingState.config,
  }

  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch {
    // Ignore storage write errors in constrained browser environments.
  }
}

export function resetTrainingRuntime(): void {
  trainingState.trainingId = null
  trainingState.trackingId = ''
  trainingState.latestResult = null
  trainingState.latestStatus = null
  trainingState.timeline = []
}
