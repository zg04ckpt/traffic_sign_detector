using Client.Components;
using Client.Services;
using System.Threading.Tasks;
using Model.Entities;

namespace Client.Views
{
    public partial class ChonMauFrm : BaseFrm
    {
        private ThongTinHL thongTinHL;
        private List<Mau> dsMauDaChon = new();

        public ChonMauFrm(ThongTinHL thongTinHL)
        {
            this.thongTinHL = thongTinHL;

            InitializeComponent();
            StartPosition = FormStartPosition.CenterScreen;
            dsMau.AutoScroll = true;

            Shown += ChonMauFrm_Shown;
        }

        private async void ChonMauFrm_Shown(object? sender, EventArgs e)
        {
            await LoadTapDuLieuAsync();
        }

        private async Task LoadTapDuLieuAsync()
        {
            dsMau.Controls.Clear();
            ShowLoading();
            try
            {
                List<TapDuLieu> dsTapDuLieu = await ClientControl.GetInstance().GetAllTapDuLieuAsync();
                cbDsTapDuLieu.Items.Clear();
                cbDsTapDuLieu.DataSource = dsTapDuLieu;
                cbDsTapDuLieu.DisplayMember = nameof(TapDuLieu.Ten);
                cbDsTapDuLieu.ValueMember = nameof(TapDuLieu.Id);
                await LoadImagesAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Lỗi khi tải danh sách mẫu: {ex.Message}", "Lỗi",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                btnTiepTuc.Enabled = true;
                HideLoading();
            }
        }

        private async Task LoadImagesAsync()
        {
            if (cbDsTapDuLieu.SelectedItem is not TapDuLieu tapDuLieu)
            {
                return;
            }

            dsMau.SuspendLayout();
            dsMau.Controls.Clear();
            tapDuLieu.DsMau.ForEach(mau =>
            {
                ImageItem image = new ImageItem(mau);
                image.OnImageSelectedChanged += Image_OnImageSelectedChanged;
                dsMau.Controls.Add(image);
            });
            dsMau.ResumeLayout();

            // Temp
            tapDuLieu.DsMau.ForEach(mau =>
            {
                Image_OnImageSelectedChanged(mau, true);
            });
        }

        private void Image_OnImageSelectedChanged(Mau mauDuocChon, bool daChon)
        {
            if (daChon)
            {
                dsMauDaChon.Add(mauDuocChon);
            }
            else
            {
                dsMauDaChon.Remove(mauDuocChon);
            }
            lbSlMauDaChon.Text = $"Đã chọn {dsMauDaChon.Count}";
        }

        private void BtnTiepTuc_Click(object sender, EventArgs e)
        {
            if (dsMauDaChon.Count == 0)
            {
                MessageBox.Show("Vui lòng chọn ít nhất 1 mẫu.", "Thông báo",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            // Tạo DS MauHL
            List<MauHL> dsMauHL = dsMauDaChon.Select(m => new MauHL
            {
                Id = m.Id,
                ThongTin = m
            }).ToList();
            thongTinHL.DsMauHL = dsMauHL;

            var frm = new CauHinhFrm(thongTinHL);
            frm.Show();

            Close();
        }
    }
}
