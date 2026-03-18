namespace Model.Entities
{
    public class ThongTinHL
    {
        public int Id { get; set; }
        public int Epochs { get; set; }
        public int BatchSize { get; set; }
        public string TrangThai { get; set; }
        public DateTime? BatDauLuc { get; set; }
        public DateTime? KetThucLuc { get; set; }
        public float DoChinhXac { get; set; }
        public float DoNhay { get; set; }
        public float LearningRate { get; set; }
        public int KichThuocAnh { get; set; }
        public string LoaiThietBi { get; set; }
        public int EarlyStoppingPatience { get; set; }
        public string Optimizer { get; set; }
        public PhienBan PhienBanHL { get; set; }
        public MoHinh MoHinhHL { get; set; }
        public List<MauHL> DsMauHL { get; set; }
        public string? DuongDanMoHinhKetQua { get; set; }
    }
}

