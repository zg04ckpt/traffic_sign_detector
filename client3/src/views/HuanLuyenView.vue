<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  createTraining,
  getLatestStatus,
  getTimeline,
  getTrainingResult,
  startTrainingSession,
} from '@/services/trainingApi'
import { resetTrainingRuntime, trainingState, type TrainingStatusEvent } from '@/state/trainingState'

const router = useRouter()

const currentEpoch = ref(0)
const isPaused = ref(false)
const isCompleted = ref(false)
const isFailed = ref(false)
const isStarting = ref(false)
const errorMessage = ref('')
const missingInputError = ref(false)
const logLines = ref<string[]>([])
const logContainer = ref<HTMLElement | null>(null)
const seenEventSignatures = new Set<string>()
let timer: number | undefined

const modelName = computed(() => trainingState.selectedModel?.Ten ?? '-')
const versionName = computed(() => trainingState.selectedVersion?.Ten ?? '-')
const totalEpoch = computed(() => Math.max(trainingState.config.epochs, 1))

const progressPercent = computed(() => {
  return Math.round((Math.min(currentEpoch.value, totalEpoch.value) / totalEpoch.value) * 100)
})

const selectedSampleDisplay = computed(() => {
  if (trainingState.selectedSampleCache.length > 0) {
    return trainingState.selectedSampleCache
  }

  return trainingState.selectedSamples.map(sample => ({
    sampleId: typeof sample.Id === 'number' ? sample.Id : null,
    sampleName: sample.DuongDanAnh
      ? sample.DuongDanAnh.split('/').pop()?.split('\\').pop() || `Mẫu #${sample.Id ?? 'N/A'}`
      : `Mẫu #${sample.Id ?? 'N/A'}`,
    datasetId: trainingState.selectedDatasetId ?? null,
    datasetName: trainingState.selectedDatasetId ? `Dataset #${trainingState.selectedDatasetId}` : 'Không xác định',
  }))
})

function appendLog(message: string) {
  logLines.value.push(message)
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

function finishTraining(state: string) {
  if (isCompleted.value) return

  isCompleted.value = true
  isFailed.value = state === 'FAILED'
  appendLog(`[${new Date().toLocaleTimeString()}] Training ${state.toLowerCase()}.`)
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }
}

function buildEventSignature(event: TrainingStatusEvent): string {
  return [event.updatedAt ?? '', event.state ?? '', event.detail ?? '', event.logLine ?? ''].join('|')
}

function applyResultState() {
  const epochValue = trainingState.latestResult?.CurrentEpoch
  if (typeof epochValue === 'number' && Number.isFinite(epochValue)) {
    currentEpoch.value = Math.max(epochValue, currentEpoch.value)
  }

  const state = String(trainingState.latestResult?.TrangThai ?? '').toUpperCase()
  if (state === 'COMPLETED' || state === 'FAILED') {
    finishTraining(state)
  }
}

async function pollTraining() {
  if (isPaused.value || isCompleted.value) return
  if (!trainingState.trainingId || !trainingState.trackingId) return

  const resultResp = await getTrainingResult(trainingState.trainingId)
  if (resultResp.ok && resultResp.data) {
    trainingState.latestResult = resultResp.data
    applyResultState()
  }

  const statusResp = await getLatestStatus(trainingState.trackingId)
  if (statusResp.ok && statusResp.data) {
    trainingState.latestStatus = statusResp.data
    if (typeof statusResp.data.currentEpoch === 'number') {
      currentEpoch.value = Math.max(currentEpoch.value, statusResp.data.currentEpoch)
    }

    const statusState = String(statusResp.data.state ?? '').toUpperCase()
    if (statusState === 'COMPLETED' || statusState === 'FAILED') {
      finishTraining(statusState)
    }
  }

  const timelineResp = await getTimeline(trainingState.trackingId)
  if (timelineResp.ok && timelineResp.data && Array.isArray(timelineResp.data.events)) {
    trainingState.timeline = timelineResp.data.events

    for (const event of timelineResp.data.events) {
      const signature = buildEventSignature(event)
      if (seenEventSignatures.has(signature)) continue

      seenEventSignatures.add(signature)
      const line = event.logLine || `${event.state ?? 'UNKNOWN'}: ${event.detail ?? ''}`
      appendLog(line)

      if (typeof event.currentEpoch === 'number') {
        currentEpoch.value = Math.max(currentEpoch.value, event.currentEpoch)
      }
    }
  }
}

async function startTrainingFlow() {
  if (isStarting.value) {
    return
  }

  if (!trainingState.selectedModel || !trainingState.selectedVersion || trainingState.selectedSamples.length === 0) {
    errorMessage.value = 'Thiếu dữ liệu huấn luyện. Vui lòng chọn mô hình, phiên bản và mẫu.'
    missingInputError.value = true
    return
  }

  isStarting.value = true
  errorMessage.value = ''
  missingInputError.value = false
  resetTrainingRuntime()
  seenEventSignatures.clear()
  logLines.value = []
  currentEpoch.value = 0

  const payload = {
    BatchSize: trainingState.config.batchSize,
    Epochs: trainingState.config.epochs,
    LearningRate: trainingState.config.learningRate,
    KichThuocAnh: trainingState.config.imageSize,
    LoaiThietBi: trainingState.config.deviceType,
    EarlyStoppingPatience: trainingState.config.earlyStoppingPatience,
    Optimizer: trainingState.config.optimizer,
    MoHinhHL: trainingState.selectedModel,
    PhienBanHL: trainingState.selectedVersion,
    DsMauHL: trainingState.selectedSamples,
  }

  const created = await createTraining(payload)
  if (!created.ok || !created.data || typeof created.data.Id !== 'number') {
    errorMessage.value = `Không tạo được phiên huấn luyện: ${created.error || 'Unknown error'}`
    appendLog(errorMessage.value)
    isStarting.value = false
    return
  }

  trainingState.trainingId = created.data.Id
  trainingState.trackingId = String(created.data.TrackingId ?? created.data.Id)
  trainingState.latestResult = created.data

  const started = await startTrainingSession(trainingState.trainingId)
  if (!started.ok) {
    errorMessage.value = `Không thể bắt đầu huấn luyện: ${started.error || 'Unknown error'}`
    appendLog(errorMessage.value)
    isStarting.value = false
    return
  }

  appendLog(`[${new Date().toLocaleTimeString()}] Training started. Tracking ID: ${trainingState.trackingId}`)
  isStarting.value = false

  await pollTraining()
  if (!isCompleted.value) {
    timer = window.setInterval(() => {
      pollTraining()
    }, 2500)
  }
}

function togglePauseResume() {
  if (isCompleted.value) return
  isPaused.value = !isPaused.value
}

function onStopTraining() {
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }

  resetTrainingRuntime()
  router.push('/quan-ly')
}

onMounted(() => {
  startTrainingFlow()
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})

function onContinue() {
  if (!isCompleted.value) return
  router.push('/ket-qua')
}

function goToModelStep() {
  router.push('/chon-mo-hinh')
}
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Huấn luyện</h3>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="missingInputError" class="mb-3">
      <button class="btn btn-outline-primary" @click="goToModelStep">Quay lại chọn dữ liệu</button>
    </div>

    <p><strong>Tên model:</strong> {{ modelName }}</p>
    <p><strong>Tên phiên bản:</strong> {{ versionName }}</p>
    <p><strong>Tracking ID:</strong> {{ trainingState.trackingId || '-' }}</p>
    <p><strong>Trạng thái:</strong> {{ trainingState.latestResult?.TrangThai || trainingState.latestStatus?.state || 'QUEUED' }}</p>
    <p>Epoch: {{ currentEpoch }}/{{ totalEpoch }}</p>

    <div id="progressBar" class="progress mb-3" role="progressbar" aria-label="Training progress">
      <div class="progress-bar" :style="{ width: `${progressPercent}%` }">
        {{ progressPercent }}%
      </div>
    </div>

    <div id="terminalLog" ref="logContainer" class="terminal-log mb-3">
      <div v-for="(line, index) in logLines" :key="index">{{ line }}</div>
    </div>

    <button id="btnPauseResume" class="btn btn-secondary" @click="onStopTraining">
      Dừng
    </button>

    <div v-if="isCompleted" class="mt-3">
      <button id="btnContinue" class="btn btn-primary" @click="onContinue">Tiếp tục</button>
      <div v-if="isFailed" class="text-danger mt-2">Huấn luyện thất bại. Kiểm tra log để biết chi tiết.</div>
    </div>

    <div class="sample-summary mt-4">
      <h5>Danh sách mẫu đã chọn</h5>
      <div v-if="selectedSampleDisplay.length > 0" class="table-responsive">
        <table class="table table-bordered table-sm">
          <thead>
            <tr>
              <th>ID mẫu</th>
              <th>Tên mẫu</th>
              <th>Tập dữ liệu</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in selectedSampleDisplay" :key="`${item.sampleId ?? 'sample'}-${index}`">
              <td>{{ item.sampleId ?? '-' }}</td>
              <td>{{ item.sampleName }}</td>
              <td>{{ item.datasetName }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else class="text-muted mb-0">Chưa có mẫu nào được chọn.</p>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  max-width: 700px;
}

.terminal-log {
  height: 220px;
  overflow: auto;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 10px;
  font-family: monospace;
  font-size: 13px;
}
</style>
