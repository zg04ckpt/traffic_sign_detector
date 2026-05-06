<template>
  <div class="app-shell">
    <main class="container py-4">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

:deep(.step-page) {
  margin-left: auto;
  margin-right: auto;
}

:deep(h3.text-center + .d-flex.flex-column.justify-content-center) {
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

</style>
