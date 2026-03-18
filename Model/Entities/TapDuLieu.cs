namespace Model.Entities
{
    public class TapDuLieu
    {
        public int Id { get; set; }
        public string Ten { get; set; }
        public List<Mau> DsMau { get; set; }
    }
}

