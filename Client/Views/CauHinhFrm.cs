using Model.Entities;
using System.Text.Json;

namespace Client.Views
{
    public partial class CauHinhFrm : Form
    {
        private ThongTinHL thongTinHL;

        public CauHinhFrm(ThongTinHL thongTinHL)
        {
            this.thongTinHL = thongTinHL;

            InitUI();
        }

        private void InitUI()
        {
            InitializeComponent();
            StartPosition = FormStartPosition.CenterScreen;
            inputBatchSize.PlaceholderText = "VD: 256";
            inputEpochs.PlaceholderText = "VD: 100";
            inputLR.PlaceholderText = "VD: 0.01";
            inputImgSize.PlaceholderText = "VD: 416";
            inputESPatience.PlaceholderText = "VD: 5";

            cbOptimizer.DataSource = new List<object>
            {
                new { Value = "Adam",    Desc = "Adam - Adaptive Moment Estimation" },
                new { Value = "SGD",     Desc = "SGD - Stochastic Gradient Descent" },
                new { Value = "AdamW",   Desc = "AdamW - Adam with Weight Decay" },
                new { Value = "NAdam",   Desc = "NAdam - Nesterov-accelerated Adam" },
                new { Value = "RAdam",   Desc = "RAdam - Rectified Adam" },
                new { Value = "RMSProp", Desc = "RMSProp - Root Mean Square Propagation" },
                new { Value = "auto",    Desc = "auto - Tự động chọn optimizer" },
            };
            cbOptimizer.DisplayMember = "Desc";
            cbOptimizer.ValueMember   = "Value";

            cbSelectDeviceType.DataSource = new List<object>
            {
                new { Name = "cpu" },
                new { Name = "gpu" },
            };
            cbSelectDeviceType.DisplayMember = "Name";
            cbSelectDeviceType.ValueMember   = "Name";

            btnTiepTuc.Click += BtnTiepTuc_Click;
        }

        private void BtnTiepTuc_Click(object? sender, EventArgs e)
        {
            if (!Validate()) return;

            try
            {
                thongTinHL.BatchSize = int.Parse(inputBatchSize.Text.Trim());
                thongTinHL.Epochs = int.Parse(inputEpochs.Text.Trim());
                thongTinHL.LearningRate = float.Parse(inputLR.Text.Trim());
                thongTinHL.KichThuocAnh = int.Parse(inputImgSize.Text.Trim());
                thongTinHL.EarlyStoppingPatience = int.Parse(inputESPatience.Text.Trim());
                thongTinHL.LoaiThietBi = cbSelectDeviceType.SelectedValue.ToString();
                thongTinHL.Optimizer = cbOptimizer.SelectedValue.ToString();
                thongTinHL.TrangThai = "Chưa bắt đầu";

                HuanLuyenFrm frm = new HuanLuyenFrm(thongTinHL);
                frm.Show();
                Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Lỗi định dạng: " + ex.Message);
                return;
            }

            Close();
        }

        private bool Validate()
        {
            if (string.IsNullOrEmpty(inputBatchSize.Text))
            {
                MessageBox.Show("Vui lòng nhập BatchSize");
                return false;
            }

            if (string.IsNullOrEmpty(inputEpochs.Text))
            {
                MessageBox.Show("Vui lòng nhập Epochs");
                return false;
            }

            if (string.IsNullOrEmpty(inputESPatience.Text))
            {
                MessageBox.Show("Vui lòng nhập ESPatience");
                return false;
            }

            if (string.IsNullOrEmpty(inputImgSize.Text))
            {
                MessageBox.Show("Vui lòng nhập kích thước ảnh");
                return false;
            }

            if (string.IsNullOrEmpty(inputLR.Text))
            {
                MessageBox.Show("Vui lòng nhập Learning Rate");
                return false;
            }

            return true;
        }

        private bool IsIntNumber(string number)
        {
            try
            {
                int.Parse(number);
                return true;
            }
            catch
            {
                return false;
            }
        }

        private bool IsFloatNumber(string number)
        {
            try
            {
                float.Parse(number);
                return true;
            }
            catch
            {
                return false;
            }
        }
    }
}

