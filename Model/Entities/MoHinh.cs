namespace Model.Entities
{
    public class MoHinh
    {
        public int Id { get; set; }
        public string Ten { get; set; }
        public string MoHinhGoc { get; set; }
        public List<PhienBan> DsPhienBan { get; set; }
    }
}

