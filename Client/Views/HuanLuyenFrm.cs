using Client.Services;
using Model.DTOs;
using Model.Entities;
using System.Windows.Forms;

namespace Client.Views
{
    public partial class HuanLuyenFrm : BaseFrm
    {
        private ThongTinHL thongTinHL;

        public HuanLuyenFrm(ThongTinHL thongTinHL)
        {
            this.thongTinHL = thongTinHL;

            InitializeComponent();
            StartPosition = FormStartPosition.CenterScreen;

            lbTenMoHinh.Text = $"Tên mô hình: {thongTinHL.MoHinhHL.Ten}";
            lbTenPhienBan.Text = $"Phiên bản được huấn luyện: {thongTinHL.PhienBanHL.Ten}";
            progressBar.Style = ProgressBarStyle.Blocks;
            progressBar.MarqueeAnimationSpeed = 30;
            progressBar.Maximum = thongTinHL.Epochs;
            btnTiepTuc.Enabled = false;
            btnHuy.Enabled = true;

            Shown += HuanLuyenFrm_Shown;
            btnHuy.Click += BtnHuy_Click;
            btnTiepTuc.Click += BtnTiepTuc_Click;
        }

        private void BtnTiepTuc_Click(object? sender, EventArgs e)
        {
            KetQuaHLFrm frm = new KetQuaHLFrm(thongTinHL.Id);
            frm.Show();
            Close();
        }

        private void BtnHuy_Click(object? sender, EventArgs e)
        {
            BackToCauHinhFrm();
        }

        private void HuanLuyenFrm_OnReceivedTrainingStatus(object? sender, TrainingStatusDto e)
        {
            UpdateStatusInUI(e.TrainingStatus, e.CurrentEpochs, e.NewLogs);

            if (e.TrainingStatus == "Hoàn thành huấn luyện")
            {
                FinishTrainingAsync();
            }
        }

        private void BackToCauHinhFrm()
        {
            var cauHinhFrm = new CauHinhFrm(thongTinHL);
            cauHinhFrm.Show();
            Close();
        }

        private void FinishTrainingAsync()
        {
            MessageBox.Show("Đã hoàn thành huấn luyện");
            btnHuy.Enabled = false;
            btnTiepTuc.Enabled = true;
        }

        private async void HuanLuyenFrm_Shown(object? sender, EventArgs e)
        {
            UpdateStatusInUI(thongTinHL.TrangThai, 0);
            await StartAsync();
        }

        private async Task StartAsync()
        {
            ShowLoading();

            try
            {
                var api = ClientControl.GetInstance();

                // Tạo thông tin huấn luyện
                thongTinHL.DsMauHL.ForEach(m => m.ThongTin.DsBien = new List<KhungNhanDang>());
                thongTinHL = await api.CreateThongTinHLAsync(thongTinHL);
                UpdateStatusInUI(thongTinHL.TrangThai, 0);

                // Cấu hình để có thể lắng nghe trạng thái huấn luyện từ server
                api.OnReceivedTrainingStatus += HuanLuyenFrm_OnReceivedTrainingStatus;
                await api.StartListeningAsync(thongTinHL.Id);
                
                // Bắt đầu huấn luyện
                await api.StartTrainingAsync(thongTinHL.Id);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Khởi động quá trình huấn luyện thất bại: " + ex.Message);

                // Quay lại trang cấu hình
                BackToCauHinhFrm();
            }
            finally
            {
                HideLoading();
            }
        }

        private void UpdateStatusInUI(string trangThaiHL, int currentEpoch, List<string>? newLogs = null)
        {
            lbTrangThai.Text = $"Trạng thái: {trangThaiHL}";
            progressBar.Value = currentEpoch;
            lbCurrentEpoch.Text = $"Epoch: {currentEpoch}/{thongTinHL.Epochs}";

            if (newLogs != null)
            {
                foreach (var log in newLogs)
                {
                    rtbLogs.AppendText(log + "\n");
                    rtbLogs.SelectionStart = rtbLogs.Text.Length;
                    rtbLogs.ScrollToCaret();
                }
            }
        }
    }
}