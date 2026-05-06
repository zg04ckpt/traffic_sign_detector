<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createDataset } from '@/services/trainingApi'

const router = useRouter()

const creating = ref(false)
const errorMessage = ref('')
const infoMessage = ref('')
const datasetName = ref('')
const imageFiles = ref<File[]>([])
const labelFiles = ref<File[]>([])

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

function onFileChange(event: Event): File[] {
  const input = event.target as HTMLInputElement
  return input.files ? Array.from(input.files) : []
}

function onImagesChange(event: Event) {
  imageFiles.value = onFileChange(event)
}

function onLabelsChange(event: Event) {
  labelFiles.value = mergeFilesByName(labelFiles.value, onFileChange(event))
}

function missingLabelNames() {
  if (imageFiles.value.length === 0) {
    return []
  }

  const labelsByBaseName = new Set(labelFiles.value.map(file => fileBaseName(file.name)))
  const missing = new Set<string>()
  for (const image of imageFiles.value) {
    const imageBaseName = fileBaseName(image.name)
    if (!labelsByBaseName.has(imageBaseName)) {
      missing.add(image.name)
    }
  }
  return Array.from(missing)
}

const createMissingLabels = computed(() => missingLabelNames())

const canCreateDataset = computed(() => {
  return datasetName.value.trim().length > 0
    && imageFiles.value.length > 0
    && labelFiles.value.length > 0
    && createMissingLabels.value.length === 0
    && !creating.value
})

function clearImages() {
  imageFiles.value = []
  labelFiles.value = []
}

function clearLabels() {
  labelFiles.value = []
}

async function onCreateDataset() {
  if (!canCreateDataset.value) {
    if (imageFiles.value.length > 0 && createMissingLabels.value.length > 0) {
      errorMessage.value = 'Bạn phải chọn đủ file label .txt cho toàn bộ ảnh (map theo cùng tên file).'
    }
    return
  }

  creating.value = true
  errorMessage.value = ''
  infoMessage.value = ''

  const result = await createDataset({
    tenDataset: datasetName.value.trim(),
    images: imageFiles.value,
    labels: labelFiles.value,
  })

  creating.value = false

  if (!result.ok || !result.data) {
    errorMessage.value = `Không tạo được dataset: ${result.error || 'Lỗi không xác định'}`
    return
  }

  infoMessage.value = 'Đã tạo dataset mới thành công.'
  router.push('/quan-ly-mau')
}

function onBackToList() {
  router.push('/quan-ly-mau')
}

function onBackToGeneralManagement() {
  router.push('/quan-ly')
}
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Thêm tập dữ liệu mới</h3>

    <div class="mb-3 d-flex gap-2">
      <button class="btn btn-outline-secondary" @click="onBackToList">Về danh sách tập dữ liệu</button>
    </div>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="infoMessage" class="alert alert-success">{{ infoMessage }}</div>

    <div class="card p-3">
      <div class="mb-2">
        <label class="form-label">Tên dataset</label>
        <input v-model="datasetName" type="text" class="form-control" placeholder="Nhập tên dataset mới">
      </div>

      <div class="mb-2">
        <label class="form-label">Ảnh mẫu (bắt buộc)</label>
        <input type="file" class="form-control" multiple accept="image/*" @change="onImagesChange">
        <small class="text-muted">Đã chọn {{ imageFiles.length }} ảnh</small>
        <button type="button" class="btn btn-sm btn-outline-secondary mt-2" :disabled="imageFiles.length === 0" @click="clearImages">
          Xóa danh sách ảnh đã chọn
        </button>
      </div>

      <div class="mb-3">
        <label class="form-label">File label .txt (bắt buộc, map theo tên ảnh)</label>
        <input type="file" class="form-control" multiple accept=".txt,text/plain" @change="onLabelsChange">
        <small class="text-muted">Đã chọn {{ labelFiles.length }} file label. Có thể chọn nhiều file hoặc tải từng file 1, hệ thống sẽ cộng dồn theo tên file.</small>
        <small v-if="imageFiles.length > 0 && createMissingLabels.length > 0" class="text-danger d-block mt-1">
          Thiếu label cho {{ createMissingLabels.length }} ảnh: {{ createMissingLabels.join(', ') }}
        </small>
        <small v-else-if="imageFiles.length > 0 && labelFiles.length > 0" class="text-success d-block mt-1">
          Đã map đủ label cho toàn bộ ảnh theo tên file.
        </small>
        <button type="button" class="btn btn-sm btn-outline-secondary mt-2" :disabled="labelFiles.length === 0" @click="clearLabels">
          Xóa danh sách label đã chọn
        </button>
      </div>

      <button class="btn btn-success" :disabled="!canCreateDataset" @click="onCreateDataset">
        {{ creating ? 'Đang tạo...' : 'Tạo dataset' }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 900px;
}
</style>
