<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { saveVersion } from '@/services/trainingApi'
import { trainingState } from '@/state/trainingState'

const router = useRouter()
const actionMessage = ref('')
const actionError = ref('')
const isSaving = ref(false)
const actionCompleted = ref(false)

const tenMoHinh = computed(() => trainingState.selectedModel?.Ten ?? '-')
const tenPhienBan = computed(() => trainingState.selectedVersion?.Ten ?? '-')
const trangThai = computed(() => trainingState.latestResult?.TrangThai ?? trainingState.latestStatus?.state ?? 'UNKNOWN')
const isFailed = computed(() => String(trangThai.value).toUpperCase() === 'FAILED')

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

const doChinhXac = computed(() => {
  const value = trainingState.latestResult?.DoChinhXac
  return typeof value === 'number' ? `${(value * 100).toFixed(2)}%` : '-'
})

const doNhay = computed(() => {
  const value = trainingState.latestResult?.DoNhay
  return typeof value === 'number' ? `${(value * 100).toFixed(2)}%` : '-'
})

const thoiGianChay = computed(() => {
  const started = trainingState.latestResult?.BatDauLuc
  const ended = trainingState.latestResult?.KetThucLuc

  if (!started || !ended) {
    return '-'
  }

  const startMs = new Date(started).getTime()
  const endMs = new Date(ended).getTime()
  if (!Number.isFinite(startMs) || !Number.isFinite(endMs) || endMs <= startMs) {
    return '-'
  }

  const totalSeconds = Math.floor((endMs - startMs) / 1000)
  const hh = String(Math.floor(totalSeconds / 3600)).padStart(2, '0')
  const mm = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
  const ss = String(totalSeconds % 60).padStart(2, '0')
  return `${hh}:${mm}:${ss}`
})

async function onSaveVersion() {
  if (isFailed.value) {
    actionError.value = 'Huấn luyện thất bại nên không thể lưu phiên bản.'
    return
  }

  actionMessage.value = ''
  actionError.value = ''

  if (!trainingState.trainingId) {
    actionError.value = 'Không có phiên huấn luyện để lưu phiên bản.'
    return
  }

  isSaving.value = true
  const result = await saveVersion(trainingState.trainingId)
  isSaving.value = false

  if (!result.ok) {
    actionError.value = `Lưu phiên bản thất bại: ${result.error || 'Unknown error'}`
    return
  }

  actionMessage.value = `Đã lưu phiên bản mới thành công (ID: ${result.data ?? 'N/A'}).`
  actionCompleted.value = true
}

function onSkipSave() {
  actionMessage.value = 'Bạn đã chọn không lưu kết quả huấn luyện.'
  actionError.value = ''
  actionCompleted.value = true
}

function onBackToManage() {
  router.push('/quan-ly')
}
</script>

<template>
  <section class="step-page">
    <h3 class="text-center mb-4">Kết quả huấn luyện</h3>

    <p><strong>Tên mô hình:</strong> <span id="lbTenMoHinh">{{ tenMoHinh }}</span></p>
    <p><strong>Tên phiên bản được huấn luyện:</strong> <span id="lbTenPhienBanHL">{{ tenPhienBan }}</span></p>

    <p><strong>Trạng thái:</strong> <span id="lbTrangThai">{{ trangThai }}</span></p>
    <p><strong>Độ chính xác:</strong> <span id="lbDoChinhXac">{{ doChinhXac }}</span></p>
    <p><strong>Độ nhạy (Recall):</strong> <span id="lbDoNhay">{{ doNhay }}</span></p>
    <p><strong>Thời gian chạy:</strong> <span id="lbThoiGianChay">{{ thoiGianChay }}</span></p>

    <div v-if="actionMessage" class="alert alert-success mt-3">{{ actionMessage }}</div>
    <div v-if="actionError" class="alert alert-danger mt-3">{{ actionError }}</div>

    <div v-if="isFailed" class="d-flex gap-2 mt-4">
      <button id="btnVeQuanLyThatBai" class="btn btn-primary" @click="onBackToManage">
        Quay về trang quản lý
      </button>
    </div>

    <div v-else-if="!actionCompleted" class="d-flex gap-2 mt-4">
      <button id="btnLuuPhienBan" class="btn btn-primary" :disabled="isSaving" @click="onSaveVersion">
        {{ isSaving ? 'Đang lưu...' : 'Lưu phiên bản mới' }}
      </button>
      <button id="btnKhongLuuPhienBan" class="btn btn-danger" @click="onSkipSave">Không lưu</button>
    </div>

    <div v-else class="d-flex gap-2 mt-4">
      <button id="btnVeQuanLy" class="btn btn-primary" @click="onBackToManage">
        Quay về trang quản lý
      </button>
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
</style>
