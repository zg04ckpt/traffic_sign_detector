namespace Model.Entities
{
    public class Mau
    {
        public int Id { get; set; }
        public string DuongDanAnh { get; set; }
        public string DoPhanGiai { get; set; }
        public List<KhungNhanDang> DsBien { get; set; }
    }
}

