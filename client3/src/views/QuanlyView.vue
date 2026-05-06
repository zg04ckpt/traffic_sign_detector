<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function onStartTraining() {
  router.push('/chon-mo-hinh')
}

function onCreateDataset() {
  router.push('/quan-ly-mau/them-moi')
}

function onManageSamples() {
  router.push('/quan-ly-mau')
}
</script>

<template>
  <section class="step-page d-flex align-items-center justify-content-center">
    <div class="action-group">
      <button id="btnStartTraining" class="btn btn-primary btn-lg px-5" @click="onStartTraining">
        Bắt đầu huấn luyện
      </button>
      <button id="btnCreateDataset" class="btn btn-success btn-lg px-5" @click="onCreateDataset">
        Tạo tập dữ liệu mới
      </button>
      <button id="btnManageSamples" class="btn btn-outline-primary btn-lg px-5" @click="onManageSamples">
        Xem danh sách mẫu huấn luyện
      </button>
    </div>
  </section>
</template>

<style scoped>
.step-page {
  min-height: calc(100vh - 120px);
}

.action-group {
  display: grid;
  gap: 12px;
}
</style>
