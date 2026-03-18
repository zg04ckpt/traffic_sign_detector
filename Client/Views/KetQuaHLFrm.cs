using Client.Services;
using System.Threading.Tasks;

namespace Client.Views
{
    public partial class KetQuaHLFrm : BaseFrm
    {
        private readonly int thongTinHLId;

        public KetQuaHLFrm(int thongTinHLId)
        {
            this.thongTinHLId = thongTinHLId;

            InitializeComponent();
            StartPosition = FormStartPosition.CenterScreen;

            Shown += (sender, e) => LoadThongTinAsync();
            btnLuuPhienBan.Click += BtnLuuPhienBan_Click;
            btnKhongLuuPhienBan.Click += BtnKhongLuuPhienBan_Click;
        }

        private void BtnKhongLuuPhienBan_Click(object? sender, EventArgs e)
        {
            BackToMain();
        }

        private async void BtnLuuPhienBan_Click(object? sender, EventArgs e)
        {
            await SaveNewPhienBanAsync();
            BackToMain();
        }

        private async Task SaveNewPhienBanAsync()
        {
            ShowLoading("Đang lưu phiên bản ...");
            var api = ClientControl.GetInstance();
            await api.CreatePhienBanFromTrainResult(thongTinHLId);
            HideLoading();

            MessageBox.Show("Lưu thành công phiên bản mới");
        }

        private async Task LoadThongTinAsync()
        {
            var api = ClientControl.GetInstance();

            var result = await api.GetTrainingResultAsync(thongTinHLId);

            lbTenMoHinh.Text = result.MoHinhHL.Ten;
            lbTenPhienBanHL.Text = result.PhienBanHL.Ten;
            lbTrangThai.Text = result.TrangThai;
            lbDoChinhXac.Text = result.DoChinhXac + "";
            lbDoNhay.Text = result.DoNhay + "";
            lbThoiGianChay.Text = 
                (result.KetThucLuc!.Value.ToLocalTime() - result.BatDauLuc!.Value.ToLocalTime()).TotalSeconds + "";
        }

        private void BackToMain()
        {
            var main = new QuanLyFrm();
            main.Show();
            Close();
        }
    }
}

