import { apiRequest, type ApiResult } from '@/services/apiClient'
import type {
  TapDuLieu,
  MoHinh,
  ThongTinHL,
  Mau,
} from '@/model'
import type {
  TrainingStatusEvent,
  TrainingStatusPayload,
} from '@/state/trainingState'

export type DatasetCreatePayload = {
  tenDataset: string
  images: File[]
  labels?: File[]
}

export type DatasetUpdatePayload = {
  tenDataset?: string
  addImages?: File[]
  labels?: File[]
  removeSampleIds?: number[]
}

function appendFiles(formData: FormData, key: string, files: File[] | undefined): void {
  if (!files || files.length === 0) {
    return
  }
  for (const file of files) {
    formData.append(key, file, file.name)
  }
}

export function getModels(): Promise<ApiResult<MoHinh[]>> {
  return apiRequest<MoHinh[]>('/api/mo-hinh')
}

export function getDatasets(): Promise<ApiResult<TapDuLieu[]>> {
  return apiRequest<TapDuLieu[]>('/api/tap-du-lieu')
}

export function getDatasetById(datasetId: number): Promise<ApiResult<TapDuLieu>> {
  return apiRequest<TapDuLieu>(`/api/tap-du-lieu/${datasetId}`)
}

export function createDataset(payload: DatasetCreatePayload): Promise<ApiResult<TapDuLieu>> {
  const formData = new FormData()
  formData.append('tenDataset', payload.tenDataset)
  appendFiles(formData, 'images', payload.images)
  appendFiles(formData, 'labels', payload.labels)

  return apiRequest<TapDuLieu>('/api/tap-du-lieu', {
    method: 'POST',
    body: formData,
  })
}

export function updateDataset(
  datasetId: number,
  payload: DatasetUpdatePayload,
): Promise<ApiResult<TapDuLieu>> {
  const formData = new FormData()

  if (payload.tenDataset && payload.tenDataset.trim().length > 0) {
    formData.append('tenDataset', payload.tenDataset.trim())
  }

  appendFiles(formData, 'addImages', payload.addImages)
  appendFiles(formData, 'labels', payload.labels)

  if (payload.removeSampleIds && payload.removeSampleIds.length > 0) {
    for (const id of payload.removeSampleIds) {
      formData.append('removeSampleIds', String(id))
    }
  }

  return apiRequest<TapDuLieu>(`/api/tap-du-lieu/${datasetId}`, {
    method: 'PUT',
    body: formData,
  })
}

export function deleteDataset(datasetId: number): Promise<ApiResult<null>> {
  return apiRequest<null>(`/api/tap-du-lieu/${datasetId}`, {
    method: 'DELETE',
  })
}

export function uploadDatasetSample(datasetId: number, file: File): Promise<ApiResult<Mau>> {
  const formData = new FormData()
  formData.append('file', file, file.name)

  return apiRequest<Mau>(`/api/tap-du-lieu/${datasetId}/upload`, {
    method: 'POST',
    body: formData,
  })
}

export function createTraining(payload: Record<string, unknown>): Promise<ApiResult<ThongTinHL>> {
  return apiRequest<ThongTinHL>('/api/huan-luyen', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
}

export function startTrainingSession(trainingId: number): Promise<ApiResult<Record<string, unknown>>> {
  return apiRequest<Record<string, unknown>>(`/api/huan-luyen/${trainingId}/bat-dau`, {
    method: 'POST',
  })
}

export function getTrainingResult(trainingId: number): Promise<ApiResult<ThongTinHL>> {
  return apiRequest<ThongTinHL>(`/api/huan-luyen/${trainingId}/ket-qua`)
}

export function getLatestStatus(trackingId: string): Promise<ApiResult<TrainingStatusPayload>> {
  return apiRequest<TrainingStatusPayload>(`/api/trainings/${encodeURIComponent(trackingId)}/status`)
}

export function getTimeline(trackingId: string): Promise<ApiResult<{ events: TrainingStatusEvent[] }>> {
  return apiRequest<{ events: TrainingStatusEvent[] }>(`/api/trainings/${encodeURIComponent(trackingId)}/events`)
}

export function saveVersion(trainingId: number): Promise<ApiResult<number>> {
  return apiRequest<number>(`/api/huan-luyen/${trainingId}/luu-phien-ban`, {
    method: 'POST',
  })
}
