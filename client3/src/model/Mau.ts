import type { KhungNhanDang } from '@/model/KhungNhanDang'

export interface Mau {
  Id: number
  DuongDanAnh: string
  DoPhanGiai?: string
  DsBien?: KhungNhanDang[]
}
