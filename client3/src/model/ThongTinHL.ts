import type { MauHL } from '@/model/MauHL'
import type { MoHinh } from '@/model/MoHinh'
import type { PhienBan } from '@/model/PhienBan'

export interface ThongTinHL {
  Id?: number
  TrackingId?: string
  TrangThai?: string
  Epochs?: number
  BatchSize?: number
  CurrentEpoch?: number
  DoChinhXac?: number
  DoNhay?: number
  BatDauLuc?: string
  KetThucLuc?: string
  LearningRate?: number
  KichThuocAnh?: number
  LoaiThietBi?: string
  EarlyStoppingPatience?: number
  Optimizer?: string
  MoHinhHL?: MoHinh
  PhienBanHL?: PhienBan
  DsMauHL?: MauHL[]
  DuongDanMoHinhKetQua?: string
}
