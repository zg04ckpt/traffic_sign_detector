import type { PhienBan } from '@/model/PhienBan'

export interface MoHinh {
  Id: number
  Ten: string
  MoHinhGoc?: string
  DsPhienBan?: PhienBan[]
}
