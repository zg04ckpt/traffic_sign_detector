<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { deleteDataset, getDatasets } from '@/services/trainingApi'
import type { TapDuLieu } from '@/model'

const router = useRouter()

const datasets = ref<TapDuLieu[]>([])
const loading = ref(false)
const deleting = ref(false)
const errorMessage = ref('')
const infoMessage = ref('')
const selectedDatasetId = ref<number | null>(null)

const selectedDataset = computed(() => datasets.value.find(item => item.Id === selectedDatasetId.value) ?? null)
const currentImages = computed(() => selectedDataset.value?.DsMau ?? [])

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
  loading.value = true
  errorMessage.value = ''

  const result = await getDatasets()
  if (!result.ok || !Array.isArray(result.data) || result.data.length === 0) {
    errorMessage.value = `Không tải được dataset: ${result.error || 'Dữ liệu rỗng'}`
    datasets.value = []
    selectedDatasetId.value = null
    loading.value = false
    return
  }

  datasets.value = result.data

  const availableIds = new Set(result.data.map(item => item.Id))
  const candidate = preferredDatasetId ?? selectedDatasetId.value ?? null

  if (candidate !== null && availableIds.has(candidate)) {
    selectedDatasetId.value = candidate
  } else {
    const firstDataset = result.data[0]
    selectedDatasetId.value = firstDataset ? firstDataset.Id : null
  }

  loading.value = false
}

function onBackToGeneralManagement() {
  router.push('/quan-ly')
}

function onUpdateDataset() {
  if (!selectedDatasetId.value) {
    return
  }

  router.push({
    path: '/quan-ly-mau/cap-nhat',
    query: { datasetId: String(selectedDatasetId.value) },
  })
}

async function onDeleteDataset() {
  if (!selectedDatasetId.value || deleting.value) {
    return
  }

  const ok = window.confirm('Bạn có chắc muốn xóa toàn bộ dataset đang chọn không?')
  if (!ok) {
    return
  }

  deleting.value = true
  errorMessage.value = ''
  infoMessage.value = ''

  const result = await deleteDataset(selectedDatasetId.value)
  deleting.value = false

  if (!result.ok) {
    errorMessage.value = `Không xóa được dataset: ${result.error || 'Lỗi không xác định'}`
    return
  }

  infoMessage.value = 'Đã xóa dataset thành công.'
  await loadDatasets(null)
}

onMounted(() => {
  loadDatasets()
})
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Danh sách tập dữ liệu</h3>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="infoMessage" class="alert alert-success">{{ infoMessage }}</div>
    <div v-if="loading" class="text-muted mb-3">Đang tải dữ liệu dataset...</div>

    <div class="toolbar mb-3">
      <button class="btn btn-outline-secondary" @click="onBackToGeneralManagement">
        Về trang quản lý chung
      </button>
      <button
        id="btnOpenUpdateDataset"
        class="btn btn-warning"
        :disabled="!selectedDatasetId"
        @click="onUpdateDataset"
      >
        Cập nhật tập dữ liệu
      </button>
    </div>

    <h5>Chọn dataset</h5>
    <div class="dataset-select-row mb-3">
      <select
        id="selectDataset"
        v-model.number="selectedDatasetId"
        class="form-control"
        :disabled="loading || datasets.length === 0"
      >
        <option v-for="dataset in datasets" :key="dataset.Id" :value="dataset.Id">
          {{ dataset.Ten }}
        </option>
      </select>
      <button
        id="btnDeleteDataset"
        class="btn btn-outline-danger"
        type="button"
        :disabled="!selectedDatasetId || deleting"
        @click="onDeleteDataset"
      >
        {{ deleting ? 'Đang xóa...' : 'Xóa dataset' }}
      </button>
    </div>

    <h6 class="mb-2">Ảnh trong dataset đang chọn</h6>
    <div id="imageGrid" class="imageGrid mb-5">
      <div v-for="image in currentImages" :key="image.Id" class="imageCard">
        <img :src="normalizeImageUrl(image.DuongDanAnh)" :alt="`Mẫu ${image.Id}`" class="thumb">
        <div class="small mt-1">Mẫu #{{ image.Id }}</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 900px;
}

.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.dataset-select-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.imageGrid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
}

.imageCard {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  padding: 8px;
}

.thumb {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
