<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDatasets } from '@/services/trainingApi'
import type { Mau, TapDuLieu } from '@/model'
import { persistTrainingSelection, trainingState } from '@/state/trainingState'

const router = useRouter()

const datasets = ref<TapDuLieu[]>([])
const loading = ref(false)
const errorMessage = ref('')

const selectedDataset = ref<number | null>(null)
const selectedImages = ref<Record<number, Mau>>({})

const currentDataset = computed(() => datasets.value.find(item => item.Id === selectedDataset.value) ?? null)

const currentImages = computed(() => {
  return currentDataset.value?.DsMau ?? []
})

const selectedCount = computed(() => Object.keys(selectedImages.value).length)
const canContinue = computed(() => selectedCount.value > 0)
const hasCurrentImages = computed(() => currentImages.value.length > 0)

function isSelected(imageId: number) {
  return selectedImages.value[imageId] !== undefined
}

function toggleImage(image: Mau) {
  const next = { ...selectedImages.value }
  if (next[image.Id]) {
    delete next[image.Id]
  } else {
    next[image.Id] = image
  }
  selectedImages.value = next
}

function selectAllCurrentImages() {
  if (!hasCurrentImages.value) {
    return
  }

  const next = { ...selectedImages.value }
  for (const image of currentImages.value) {
    next[image.Id] = image
  }
  selectedImages.value = next
}

function clearAllCurrentImages() {
  if (!hasCurrentImages.value) {
    return
  }

  const next = { ...selectedImages.value }
  for (const image of currentImages.value) {
    delete next[image.Id]
  }
  selectedImages.value = next
}

function reconcileSelectedSamples() {
  const allSamples = new Map<number, Mau>()
  for (const dataset of datasets.value) {
    for (const sample of dataset.DsMau ?? []) {
      allSamples.set(sample.Id, sample)
    }
  }

  const next: Record<number, Mau> = {}
  for (const value of Object.values(selectedImages.value)) {
    const match = allSamples.get(value.Id)
    if (match) {
      next[value.Id] = match
    }
  }
  selectedImages.value = next
}

function normalizeImageUrl(value: string | undefined) {
  if (!value) {
    return ''
  }

  if (value.startsWith('http://') || value.startsWith('https://')) {
    return value
  }

  return value.startsWith('/') ? value : `/${value}`
}

async function loadDatasets(preferredDatasetId: number | null = null) {
  if (!trainingState.selectedModel || !trainingState.selectedVersion) {
    errorMessage.value = 'Vui lòng chọn mô hình trước khi chọn mẫu.'
    return
  }

  loading.value = true
  errorMessage.value = ''

  const result = await getDatasets()
  if (!result.ok || !Array.isArray(result.data) || result.data.length === 0) {
    errorMessage.value = `Không tải được dataset: ${result.error || 'Dữ liệu rỗng'}`
    datasets.value = []
    selectedDataset.value = null
    loading.value = false
    return
  }

  datasets.value = result.data

  const availableIds = new Set(result.data.map(item => item.Id))
  const candidate = preferredDatasetId
    ?? selectedDataset.value
    ?? trainingState.selectedDatasetId
    ?? null

  if (candidate !== null && availableIds.has(candidate)) {
    selectedDataset.value = candidate
  } else {
    const firstDataset = result.data[0]
    selectedDataset.value = firstDataset ? firstDataset.Id : null
  }

  reconcileSelectedSamples()
  loading.value = false
}

function onContinue() {
  if (!canContinue.value) return

  trainingState.selectedDatasetId = selectedDataset.value
  trainingState.selectedSamples = Object.values(selectedImages.value)
  persistTrainingSelection()
  router.push('/cau-hinh')
}

onMounted(() => {
  loadDatasets()
})
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Chọn mẫu dữ liệu huấn luyện</h3>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="loading" class="text-muted mb-3">Đang tải dữ liệu dataset...</div>

    <h5>Chọn dataset</h5>
    <div class="dataset-select-row mb-3">
      <select id="selectDataset" v-model.number="selectedDataset" class="form-control" :disabled="loading || datasets.length === 0">
        <option v-for="dataset in datasets" :key="dataset.Id" :value="dataset.Id">
          {{ dataset.Ten }}
        </option>
      </select>
    </div>

    <div class="selection-toolbar mb-3">
      <p id="lbSelectedCount" class="mb-0">Số mẫu đã chọn: {{ selectedCount }}</p>
      <div class="d-flex gap-2">
        <button
          id="btnSelectAll"
          type="button"
          class="btn btn-sm btn-outline-primary"
          :disabled="!hasCurrentImages"
          @click="selectAllCurrentImages"
        >
          Chọn tất cả
        </button>
        <button
          id="btnClearSelected"
          type="button"
          class="btn btn-sm btn-outline-secondary"
          :disabled="!hasCurrentImages"
          @click="clearAllCurrentImages"
        >
          Bỏ chọn
        </button>
      </div>
    </div>

    <div id="imageGrid" class="imageGrid mb-5">
      <div
        v-for="image in currentImages"
        :key="image.Id"
        class="imageCard"
        :class="{ selected: isSelected(image.Id) }"
      >
        <img :src="normalizeImageUrl(image.DuongDanAnh)" :alt="`Mẫu ${image.Id}`" class="thumb">
        <div class="small mt-1">Mẫu #{{ image.Id }}</div>
        <div class="card-actions mt-2">
          <button
            class="btn btn-sm"
            :class="isSelected(image.Id) ? 'btn-primary' : 'btn-outline-primary'"
            @click="toggleImage(image)"
          >
            {{ isSelected(image.Id) ? 'Bỏ chọn train' : 'Chọn train' }}
          </button>
        </div>
      </div>
    </div>

    <div class="footerAction">
      <button id="btnContinue" class="btn btn-primary" :disabled="!canContinue" @click="onContinue">
        Tiếp tục
      </button>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 900px;
  padding-bottom: 90px;
}

.imageGrid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.selection-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.imageCard {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  padding: 8px;
}

.imageCard.selected {
  border-color: #0d6efd;
  box-shadow: 0 0 0 1px #0d6efd;
}

.card-actions {
  display: grid;
  gap: 6px;
}

.dataset-select-row {
  max-width: 520px;
}

.thumb {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.footerAction {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}
</style>
