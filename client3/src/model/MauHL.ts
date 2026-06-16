import type { KhungNhanDang } from '@/model/KhungNhanDang'
import type { Mau } from '@/model/Mau'

/** Matches shared-model MauHL (JSON PascalCase). */
export interface MauHL {
  Id?: number
  ThongTin?: Mau
  DuongDanAnh?: string
  DoPhanGiai?: string
  DsBien?: KhungNhanDang[]
}
