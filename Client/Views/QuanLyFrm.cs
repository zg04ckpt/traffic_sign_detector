namespace Client.Views
{
    public partial class QuanLyFrm : Form
    {
        public QuanLyFrm()
        {
            InitializeComponent();

            StartPosition = FormStartPosition.CenterScreen;
        }

        private void btnHuanLuyenNhanDangVung_Click(object sender, EventArgs e)
        {
            var chonMoHinhWin = new ChonMoHinhFrm();
            chonMoHinhWin.Show();
            Close();
        }
    }
}