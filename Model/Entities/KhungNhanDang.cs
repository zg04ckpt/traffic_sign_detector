namespace Model.Entities
{
    public class KhungNhanDang
    {
        public int Id { get; set; }
        public float XCenter { get; set; }
        public float YCenter { get; set; }
        public float W { get; set; }
        public float H { get; set; }
        public LoaiBien Bien { get; set; }
    }
}

