using Client.Services;

namespace Client.Views
{
    partial class HuanLuyenFrm
    {
        private System.ComponentModel.IContainer components = null;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null)) components.Dispose();
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            label1 = new Label();
            lbTenMoHinh = new Label();
            lbTrangThai = new Label();
            progressBar = new ProgressBar();
            rtbLogs = new RichTextBox();
            btnHuy = new Button();
            lbCurrentEpoch = new Label();
            lbTenPhienBan = new Label();
            btnTiepTuc = new Button();
            SuspendLayout();
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Font = new Font("Segoe UI", 12F, FontStyle.Bold, GraphicsUnit.Point);
            label1.Location = new Point(267, 9);
            label1.Name = "label1";
            label1.Size = new Size(191, 21);
            label1.TabIndex = 5;
            label1.Text = "HUẤN LUYỆN MÔ HÌNH";
            // 
            // lbTenMoHinh
            // 
            lbTenMoHinh.AutoSize = true;
            lbTenMoHinh.Location = new Point(12, 37);
            lbTenMoHinh.Name = "lbTenMoHinh";
            lbTenMoHinh.Size = new Size(80, 15);
            lbTenMoHinh.TabIndex = 4;
            lbTenMoHinh.Text = "Tên mô hình: ";
            // 
            // lbTrangThai
            // 
            lbTrangThai.AutoSize = true;
            lbTrangThai.Location = new Point(12, 78);
            lbTrangThai.Name = "lbTrangThai";
            lbTrangThai.Size = new Size(66, 15);
            lbTrangThai.TabIndex = 3;
            lbTrangThai.Text = "Trạng thái: ";
            // 
            // progressBar
            // 
            progressBar.Location = new Point(12, 106);
            progressBar.Name = "progressBar";
            progressBar.Size = new Size(760, 20);
            progressBar.TabIndex = 2;
            // 
            // rtbLogs
            // 
            rtbLogs.BackColor = Color.Black;
            rtbLogs.Font = new Font("Consolas", 9.75F, FontStyle.Regular, GraphicsUnit.Point);
            rtbLogs.ForeColor = Color.White;
            rtbLogs.Location = new Point(12, 132);
            rtbLogs.Name = "rtbLogs";
            rtbLogs.ReadOnly = true;
            rtbLogs.ScrollBars = RichTextBoxScrollBars.Vertical;
            rtbLogs.Size = new Size(760, 270);
            rtbLogs.TabIndex = 1;
            rtbLogs.Text = "";
            // 
            // btnHuy
            // 
            btnHuy.BackColor = Color.White;
            btnHuy.ForeColor = Color.Black;
            btnHuy.Location = new Point(262, 408);
            btnHuy.Name = "btnHuy";
            btnHuy.Size = new Size(120, 30);
            btnHuy.TabIndex = 0;
            btnHuy.Text = "Hủy";
            btnHuy.UseVisualStyleBackColor = false;
            // 
            // lbCurrentEpoch
            // 
            lbCurrentEpoch.AutoSize = true;
            lbCurrentEpoch.Location = new Point(697, 78);
            lbCurrentEpoch.Name = "lbCurrentEpoch";
            lbCurrentEpoch.Size = new Size(75, 15);
            lbCurrentEpoch.TabIndex = 6;
            lbCurrentEpoch.Text = "Epoch: 10/20";
            lbCurrentEpoch.TextAlign = ContentAlignment.MiddleRight;
            // 
            // lbTenPhienBan
            // 
            lbTenPhienBan.AutoSize = true;
            lbTenPhienBan.Location = new Point(12, 57);
            lbTenPhienBan.Name = "lbTenPhienBan";
            lbTenPhienBan.Size = new Size(158, 15);
            lbTenPhienBan.TabIndex = 7;
            lbTenPhienBan.Text = "Phiên bản được huấn luyện: ";
            // 
            // btnTiepTuc
            // 
            btnTiepTuc.Enabled = false;
            btnTiepTuc.Location = new Point(388, 408);
            btnTiepTuc.Name = "btnTiepTuc";
            btnTiepTuc.Size = new Size(120, 30);
            btnTiepTuc.TabIndex = 8;
            btnTiepTuc.Text = "Tiếp tục";
            // 
            // HuanLuyenFrm
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(800, 450);
            Controls.Add(btnTiepTuc);
            Controls.Add(lbTenPhienBan);
            Controls.Add(lbCurrentEpoch);
            Controls.Add(btnHuy);
            Controls.Add(rtbLogs);
            Controls.Add(progressBar);
            Controls.Add(lbTrangThai);
            Controls.Add(lbTenMoHinh);
            Controls.Add(label1);
            Name = "HuanLuyenFrm";
            Text = "Ứng dụng nhận dạng biển số - Huấn luyện";
            ResumeLayout(false);
            PerformLayout();
        }

        private Label label1;
        private Label lbTenMoHinh;
        private Label lbTrangThai;
        private ProgressBar progressBar;
        private RichTextBox rtbLogs;
        private Button btnHuy;
        private Label lbCurrentEpoch;
        private Label lbTenPhienBan;
        private Button btnTiepTuc;
    }
}
