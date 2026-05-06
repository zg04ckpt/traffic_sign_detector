<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { persistTrainingSelection, trainingState } from '@/state/trainingState'

const router = useRouter()

const batchSize = ref(256)
const epochs = ref(2)
const learningRate = ref(0.01)
const imageSize = ref(416)
const earlyStoppingPatience = ref(5)
const deviceType = ref('cpu')
const optimizer = ref('Adam')

const canContinue = computed(() => {
    return (
        batchSize.value > 0 &&
        epochs.value > 0 &&
        learningRate.value > 0 &&
        imageSize.value > 0 &&
        earlyStoppingPatience.value >= 0 &&
        deviceType.value !== '' &&
        optimizer.value !== ''
    )
})

function onContinue() {
    if (!canContinue.value) return

    trainingState.config = {
        batchSize: batchSize.value,
        epochs: epochs.value,
        learningRate: learningRate.value,
        imageSize: imageSize.value,
        earlyStoppingPatience: earlyStoppingPatience.value,
        deviceType: deviceType.value,
        optimizer: optimizer.value,
    }

    persistTrainingSelection()

    router.push('/huan-luyen')
}
</script>

<template>
    <section class="step-page">
        <h3 class="text-center mb-4">Cấu hình tham số</h3>
        <div class="d-flex flex-column">
            <h5>Batch Size</h5>
            <input id="batchSize" v-model.number="batchSize" class="form-control mb-3" type="number" min="1">

            <h5>Epochs</h5>
            <input id="epochs" v-model.number="epochs" class="form-control mb-3" type="number" min="1">

            <h5>Learning Rate</h5>
            <input
                id="learningRate"
                v-model.number="learningRate"
                class="form-control mb-3"
                type="number"
                min="0.000001"
                step="0.0001"
            >

            <h5>Kích Thước Ảnh</h5>
            <input id="imageSize" v-model.number="imageSize" class="form-control mb-3" type="number" min="1">

            <h5>Early Stopping Patience</h5>
            <input
                id="earlyStoppingPatience"
                v-model.number="earlyStoppingPatience"
                class="form-control mb-3"
                type="number"
                min="0"
            >

            <h5>Loại thiết bị</h5>
            <select id="deviceType" v-model="deviceType" class="form-control mb-3">
                <option value="cpu">cpu</option>
                <option value="gpu">gpu</option>
            </select>

            <h5>Optimizer</h5>
            <select id="optimizer" v-model="optimizer" class="form-control mb-5">
                <option value="Adam">Adam</option>
                <option value="SGD">SGD</option>
                <option value="RMSprop">RMSprop</option>
            </select>
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
    max-width: 700px;
    padding-bottom: 90px;
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