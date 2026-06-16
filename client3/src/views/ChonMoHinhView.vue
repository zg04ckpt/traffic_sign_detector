<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getModels } from '@/services/trainingApi'
import type { MoHinh, PhienBan } from '@/model'
import { persistTrainingSelection, trainingState } from '@/state/trainingState'

const router = useRouter()

const models = ref<MoHinh[]>([])
const loading = ref(false)
const errorMessage = ref('')

const selectedModelId = ref<number | null>(null)
const selectedVersionId = ref<number | null>(null)

const versions = computed<PhienBan[]>(() => {
  const model = models.value.find(item => item.Id === selectedModelId.value)
  return model?.DsPhienBan ?? []
})

const canContinue = computed(() => {
  return selectedModelId.value !== null && selectedVersionId.value !== null && !loading.value
})

function syncVersion() {
  const availableVersions = versions.value
  if (availableVersions.length === 0) {
    selectedVersionId.value = null
    return
  }

  const currentExists = availableVersions.some(version => version.Id === selectedVersionId.value)
  if (!currentExists) {
    const firstVersion = availableVersions[0]
    selectedVersionId.value = firstVersion ? firstVersion.Id : null
  }
}

function onModelChanged() {
  syncVersion()
}

async function loadModelCatalog() {
  loading.value = true
  errorMessage.value = ''

  const result = await getModels()
  if (!result.ok || !Array.isArray(result.data) || result.data.length === 0) {
    errorMessage.value = `Không tải được danh sách mô hình: ${result.error || 'Dữ liệu rỗng'}`
    models.value = []
    selectedModelId.value = null
    selectedVersionId.value = null
    loading.value = false
    return
  }

  models.value = result.data
  const firstModel = result.data[0]
  selectedModelId.value = firstModel ? firstModel.Id : null
  syncVersion()
  loading.value = false
}

function onContinue() {
  if (!canContinue.value) {
    return
  }

  const model = models.value.find(item => item.Id === selectedModelId.value) ?? null
  const version = versions.value.find(item => item.Id === selectedVersionId.value) ?? null

  if (!model || !version) {
    errorMessage.value = 'Vui lòng chọn mô hình và phiên bản hợp lệ.'
    return
  }

  trainingState.selectedModel = model
  trainingState.selectedVersion = version
  persistTrainingSelection()
  router.push('/chon-mau')
}

onMounted(() => {
  loadModelCatalog()
})
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Chọn nguồn huấn luyện</h3>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="loading" class="text-muted mb-3">Đang tải dữ liệu mô hình...</div>

    <div class="d-flex flex-column justify-content-center">
      <h5>Chọn mô hình</h5>
      <select
        id="modelSelect"
        v-model.number="selectedModelId"
        class="form-control mb-3"
        :disabled="loading || models.length === 0"
        @change="onModelChanged"
      >
        <option v-for="model in models" :key="model.Id" :value="model.Id">
          {{ model.Ten }}
        </option>
      </select>

      <h5>Chọn phiên bản</h5>
      <select
        id="versionSelect"
        v-model.number="selectedVersionId"
        class="form-control mb-3"
        :disabled="loading || versions.length === 0"
      >
        <option v-for="version in versions" :key="version.Id" :value="version.Id">
          {{ version.Ten }}
        </option>
      </select>

      <div class="d-flex justify-content-center">
        <button id="btnContinue" class="btn btn-primary" :disabled="!canContinue" @click="onContinue">
          Tiếp tục
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 700px;
}
</style>
