
import CauHinhView from '@/views/CauHinhView.vue'
import CapNhatTapDuLieuView from '@/views/CapNhatTapDuLieuView.vue'
import ChonMauView from '@/views/ChonMauView.vue'
import ChonMoHinhView from '@/views/ChonMoHinhView.vue'
import DSTapDuLieuVIew from '@/views/DSTapDuLieuVIew.vue'
import HuanLuyenView from '@/views/HuanLuyenView.vue'
import KetQuaHLView from '@/views/KetQuaHLView.vue'
import QuanlyView from '@/views/QuanlyView.vue'
import ThemMoiTapDuLieuView from '@/views/ThemMoiTapDuLieuView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/quan-ly',
    },
    {
      path: '/quan-ly',
      name: 'quan-ly',
      component: QuanlyView,
    },
    {
      path: '/chon-mo-hinh',
      name: 'chon-mo-hinh',
      component: ChonMoHinhView,
    },
    {
      path: '/chon-mau',
      name: 'chon-mau',
      component: ChonMauView,
    },
    {
      path: '/quan-ly-mau',
      name: 'quan-ly-mau',
      component: DSTapDuLieuVIew,
    },
    {
      path: '/quan-ly-mau/them-moi',
      name: 'them-moi-tap-du-lieu',
      component: ThemMoiTapDuLieuView,
    },
    {
      path: '/quan-ly-mau/cap-nhat',
      name: 'cap-nhat-tap-du-lieu',
      component: CapNhatTapDuLieuView,
    },
    {
      path: '/cau-hinh',
      name: 'cau-hinh',
      component: CauHinhView,
    },
    {
      path: '/huan-luyen',
      name: 'huan-luyen',
      component: HuanLuyenView,
    },
    {
      path: '/ket-qua',
      name: 'ket-qua',
      component: KetQuaHLView,
    },
  ],
})

export default router
