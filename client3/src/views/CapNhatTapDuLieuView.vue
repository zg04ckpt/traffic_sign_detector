<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDatasetById, updateDataset } from '@/services/trainingApi'
import type { TapDuLieu } from '@/model'

const route = useRoute()
const router = useRouter()

const currentDataset = ref<TapDuLieu | null>(null)
const loading = ref(false)
const updating = ref(false)
const errorMessage = ref('')
const infoMessage = ref('')

const selectedDatasetId = ref<number | null>(null)
const updateDatasetName = ref('')
const addImageFiles = ref<File[]>([])
const addLabelFiles = ref<File[]>([])
const removeSampleIds = ref<Set<number>>(new Set())

const currentImages = computed(() => currentDataset.value?.DsMau ?? [])
const removeCount = computed(() => removeSampleIds.value.size)
const selectedDatasetIdLabel = computed(() => selectedDatasetId.value ?? 'Chưa xác định')

watch(currentDataset, dataset => {
  updateDatasetName.value = dataset?.Ten ?? ''
  removeSampleIds.value = new Set()
})

function parseDatasetId(value: unknown): number | null {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

function fileBaseName(fileName: string) {
  const dotIndex = fileName.lastIndexOf('.')
  const normalized = dotIndex > 0 ? fileName.slice(0, dotIndex) : fileName
  return normalized.trim().toLowerCase()
}

function mergeFilesByName(currentFiles: File[], incomingFiles: File[]) {
  const merged = new Map<string, File>()
  for (const file of currentFiles) {
    merged.set(file.name.toLowerCase(), file)
  }
  for (const file of incomingFiles) {
    merged.set(file.name.toLowerCase(), file)
  }
  return Array.from(merged.values())
}

function missingLabelNames(imageFiles: File[], labelFiles: File[]) {
  if (imageFiles.length === 0) {
    return []
  }

  const labelsByBaseName = new Set(labelFiles.map(file => fileBaseName(file.name)))
  const missing = new Set<string>()
  for (const image of imageFiles) {
    const imageBaseName = fileBaseName(image.name)
    if (!labelsByBaseName.has(imageBaseName)) {
      missing.add(image.name)
    }
  }
  return Array.from(missing)
}

const addMissingLabels = computed(() => missingLabelNames(addImageFiles.value, addLabelFiles.value))

const isAddLabelComplete = computed(() => {
  if (addImageFiles.value.length === 0) {
    return true
  }
  return addLabelFiles.value.length > 0 && addMissingLabels.value.length === 0
})

const canUpdateDataset = computed(() => {
  if (!currentDataset.value || updating.value) {
    return false
  }

  const nameChanged = updateDatasetName.value.trim().length > 0
    && updateDatasetName.value.trim() !== currentDataset.value.Ten

  return isAddLabelComplete.value && (
    nameChanged
    || addImageFiles.value.length > 0
    || removeSampleIds.value.size > 0
  )
})

function onFileChange(event: Event): File[] {
  const input = event.target as HTMLInputElement
  return input.files ? Array.from(input.files) : []
}

function onAddImagesChange(event: Event) {
  addImageFiles.value = onFileChange(event)
}

function onAddLabelsChange(event: Event) {
  addLabelFiles.value = mergeFilesByName(addLabelFiles.value, onFileChange(event))
}

function clearAddImages() {
  addImageFiles.value = []
  addLabelFiles.value = []
}

function clearAddLabels() {
  addLabelFiles.value = []
}

function resetUpdateForm() {
  addImageFiles.value = []
  addLabelFiles.value = []
  removeSampleIds.value = new Set()
}

function syncSelectedDatasetFromRoute() {
  selectedDatasetId.value = parseDatasetId(route.query.datasetId)
}

function isMarkedToRemove(imageId: number) {
  return removeSampleIds.value.has(imageId)
}

function toggleRemoveSample(imageId: number) {
  const next = new Set(removeSampleIds.value)
  if (next.has(imageId)) {
    next.delete(imageId)
  } else {
    next.add(imageId)
  }
  removeSampleIds.value = next
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

async function loadDatasetById() {
  loading.value = true
  errorMessage.value = ''

  if (selectedDatasetId.value === null) {
    errorMessage.value = 'Thiếu datasetId trên đường dẫn cập nhật.'
    currentDataset.value = null
    loading.value = false
    return
  }

  const result = await getDatasetById(selectedDatasetId.value)
  if (!result.ok || !result.data) {
    errorMessage.value = `Không tải được dataset: ${result.error || 'Lỗi không xác định'}`
    currentDataset.value = null
    loading.value = false
    return
  }

  currentDataset.value = result.data

  loading.value = false
}

async function onUpdateDataset() {
  if (!selectedDatasetId.value || !canUpdateDataset.value) {
    if (addImageFiles.value.length > 0 && !isAddLabelComplete.value) {
      errorMessage.value = 'Bạn phải chọn đủ label .txt cho danh sách ảnh thêm mới (map theo cùng tên file).'
    }
    return
  }

  updating.value = true
  errorMessage.value = ''
  infoMessage.value = ''

  const result = await updateDataset(selectedDatasetId.value, {
    tenDataset: updateDatasetName.value.trim(),
    addImages: addImageFiles.value,
    labels: addLabelFiles.value,
    removeSampleIds: Array.from(removeSampleIds.value),
  })

  updating.value = false

  if (!result.ok || !result.data) {
    errorMessage.value = `Không cập nhật được dataset: ${result.error || 'Lỗi không xác định'}`
    return
  }

  infoMessage.value = 'Đã cập nhật dataset thành công.'
  resetUpdateForm()
  router.push('/quan-ly-mau')
}

function onBackToList() {
  router.push('/quan-ly-mau')
}

function onBackToGeneralManagement() {
  router.push('/quan-ly')
}

function onOpenCreate() {
  router.push('/quan-ly-mau/them-moi')
}

onMounted(() => {
  syncSelectedDatasetFromRoute()
  loadDatasetById()
})

watch(
  () => route.query.datasetId,
  () => {
    syncSelectedDatasetFromRoute()
    loadDatasetById()
  }
)
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Cập nhật tập dữ liệu</h3>

    <div class="mb-3 d-flex gap-2 flex-wrap">
      <button class="btn btn-outline-secondary" @click="onBackToList">Về danh sách tập dữ liệu</button>
    </div>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="infoMessage" class="alert alert-success">{{ infoMessage }}</div>
    <div v-if="loading" class="text-muted mb-3">Đang tải dữ liệu dataset...</div>

    <div class="dataset-tools mb-3">
      <div class="card p-3">
        <h6 class="mb-3">Cập nhật dataset đang chọn</h6>
        <div class="mb-3 alert alert-light border">
          <div><strong>ID:</strong> {{ selectedDatasetIdLabel }}</div>
          <div><strong>Tên:</strong> {{ currentDataset?.Ten || 'Chưa có dữ liệu' }}</div>
        </div>
        <div class="mb-2">
          <label class="form-label">Đổi tên dataset</label>
          <input v-model="updateDatasetName" type="text" class="form-control" placeholder="Tên dataset mới">
        </div>
        <div class="mb-2">
          <label class="form-label">Thêm ảnh mới</label>
          <input type="file" class="form-control" multiple accept="image/*" @change="onAddImagesChange">
          <small class="text-muted">Đã chọn {{ addImageFiles.length }} ảnh để thêm</small>
          <button type="button" class="btn btn-sm btn-outline-secondary mt-2" :disabled="addImageFiles.length === 0" @click="clearAddImages">
            Xóa danh sách ảnh thêm mới
          </button>
        </div>
        <div class="mb-2">
          <label class="form-label">Label .txt cho ảnh thêm mới</label>
          <input type="file" class="form-control" multiple accept=".txt,text/plain" @change="onAddLabelsChange">
          <small class="text-muted">Đã chọn {{ addLabelFiles.length }} file label. Có thể chọn nhiều file hoặc tải từng file 1, hệ thống sẽ cộng dồn theo tên file.</small>
          <small v-if="addImageFiles.length > 0 && addMissingLabels.length > 0" class="text-danger d-block mt-1">
            Thiếu label cho {{ addMissingLabels.length }} ảnh thêm mới: {{ addMissingLabels.join(', ') }}
          </small>
          <small v-else-if="addImageFiles.length > 0 && addLabelFiles.length > 0" class="text-success d-block mt-1">
            Đã map đủ label cho danh sách ảnh thêm mới.
          </small>
          <button type="button" class="btn btn-sm btn-outline-secondary mt-2" :disabled="addLabelFiles.length === 0" @click="clearAddLabels">
            Xóa danh sách label thêm mới
          </button>
        </div>
        <p class="mb-3 text-muted">Ảnh đánh dấu xóa: {{ removeCount }}</p>
        <button class="btn btn-warning" :disabled="!canUpdateDataset" @click="onUpdateDataset">
          {{ updating ? 'Đang cập nhật...' : 'Lưu cập nhật dataset' }}
        </button>
      </div>
    </div>

    <div id="imageGrid" class="imageGrid mb-5">
      <div
        v-for="image in currentImages"
        :key="image.Id"
        class="imageCard"
        :class="{ removing: isMarkedToRemove(image.Id) }"
      >
        <img :src="normalizeImageUrl(image.DuongDanAnh)" :alt="`Mẫu ${image.Id}`" class="thumb">
        <div class="small mt-1">Mẫu #{{ image.Id }}</div>
        <div class="card-actions mt-2">
          <button
            class="btn btn-sm"
            :class="isMarkedToRemove(image.Id) ? 'btn-danger' : 'btn-outline-danger'"
            @click="toggleRemoveSample(image.Id)"
          >
            {{ isMarkedToRemove(image.Id) ? 'Bỏ đánh dấu xóa' : 'Đánh dấu xóa' }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 900px;
}

.dataset-tools {
  display: grid;
  gap: 12px;
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

.imageCard.removing {
  border-color: #dc3545;
  box-shadow: 0 0 0 1px #dc3545;
  background: #fff5f5;
}

.card-actions {
  display: grid;
  gap: 6px;
}

.thumb {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}
</style>
