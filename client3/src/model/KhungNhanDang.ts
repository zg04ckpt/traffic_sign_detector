import type { LoaiBien } from '@/model/LoaiBien'

export interface KhungNhanDang {
  Id: number
  XCenter: number
  YCenter: number
  W: number
  H: number
  Bien: LoaiBien
}
