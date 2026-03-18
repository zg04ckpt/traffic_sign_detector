using Client.Components;
using Client.Services;
using Model;
using Model.Entities;

namespace Client.Views
{
    public partial class ChonMoHinhFrm : BaseFrm
    {
        public ChonMoHinhFrm()
        {
            InitializeComponent();

            StartPosition = FormStartPosition.CenterScreen;

            cbDsMoHinh.SelectedIndexChanged += (_, _) => LoadVersions();
            btnTiepTuc.Click += BtnTiepTuc_Click;
            Shown += ChonMoHinhFrm_Shown; 
        }

        private async void ChonMoHinhFrm_Shown(object? sender, EventArgs e)
        {
            await LoadModelsAsync();
        }

        private async Task LoadModelsAsync()
        {
            ShowLoading();
            try
            {
                List<MoHinh> dsMh = await ClientControl.GetInstance().GetAllMoHinhAsync();
                cbDsMoHinh.DataSource = dsMh;
                cbDsMoHinh.DisplayMember = nameof(MoHinh.Ten);
                cbDsMoHinh.ValueMember = nameof(MoHinh.Id);
                LoadVersions();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Lỗi tải danh sách mô hình: {ex.Message}", "Lỗi",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                HideLoading();
            }
        }

        private void LoadVersions()
        {
            if (cbDsMoHinh.SelectedItem is not MoHinh mh) return;

            var versions = mh.DsPhienBan;
            cbDsPhienBan.DataSource = versions;
            cbDsPhienBan.DisplayMember = nameof(PhienBan.Ten);
            cbDsPhienBan.ValueMember = nameof(PhienBan.Id);
        }

        private void BtnTiepTuc_Click(object? sender, EventArgs e)
        {
            if (cbDsMoHinh.SelectedItem is not MoHinh selectedMoHinh)
            {
                MessageBox.Show("Vui lòng chọn mô hình.", "Thông báo",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            if (cbDsPhienBan.SelectedItem is not PhienBan selectedPhienBan)
            {
                MessageBox.Show("Vui lòng chọn phiên bản của mô hình.", "Thông báo",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            var thongTinHL = new ThongTinHL
            {
                MoHinhHL = selectedMoHinh,
                PhienBanHL = selectedPhienBan,
            };

            var frm = new ChonMauFrm(thongTinHL);
            frm.Show();
            Close();
        }
    }
}
