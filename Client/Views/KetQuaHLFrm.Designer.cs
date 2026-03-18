namespace Client.Views
{
    partial class KetQuaHLFrm
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
            lbCorrect = new Label();
            lbRecall = new Label();
            lbTime = new Label();
            lbModel = new Label();
            btnLuuPhienBan = new Button();
            btnKhongLuuPhienBan = new Button();
            label2 = new Label();
            label3 = new Label();
            label4 = new Label();
            label5 = new Label();
            lbTenMoHinh = new Label();
            lbTenPhienBanHL = new Label();
            lbTrangThai = new Label();
            lbDoChinhXac = new Label();
            lbDoNhay = new Label();
            lbThoiGianChay = new Label();
            SuspendLayout();
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Font = new Font("Segoe UI", 12F, FontStyle.Bold, GraphicsUnit.Point);
            label1.Location = new Point(354, 24);
            label1.Name = "label1";
            label1.Size = new Size(231, 28);
            label1.TabIndex = 6;
            label1.Text = "KẾT QUẢ HUẤN LUYỆN";
            // 
            // lbCorrect
            // 
            lbCorrect.AutoSize = true;
            lbCorrect.Location = new Point(162, 206);
            lbCorrect.Name = "lbCorrect";
            lbCorrect.Size = new Size(88, 20);
            lbCorrect.TabIndex = 5;
            lbCorrect.Text = "- Trạng thái:";
            // 
            // lbRecall
            // 
            lbRecall.AutoSize = true;
            lbRecall.Location = new Point(162, 246);
            lbRecall.Name = "lbRecall";
            lbRecall.Size = new Size(104, 20);
            lbRecall.TabIndex = 4;
            lbRecall.Text = "- Độ chính xác";
            // 
            // lbTime
            // 
            lbTime.AutoSize = true;
            lbTime.Location = new Point(162, 286);
            lbTime.Name = "lbTime";
            lbTime.Size = new Size(131, 20);
            lbTime.TabIndex = 3;
            lbTime.Text = "- Độ nhạy (Recall):";
            // 
            // lbModel
            // 
            lbModel.AutoSize = true;
            lbModel.Location = new Point(162, 326);
            lbModel.MaximumSize = new Size(823, 0);
            lbModel.Name = "lbModel";
            lbModel.Size = new Size(118, 20);
            lbModel.TabIndex = 2;
            lbModel.Text = "- Thời gian chạy:";
            // 
            // btnLuuPhienBan
            // 
            btnLuuPhienBan.Location = new Point(230, 533);
            btnLuuPhienBan.Margin = new Padding(3, 4, 3, 4);
            btnLuuPhienBan.Name = "btnLuuPhienBan";
            btnLuuPhienBan.Size = new Size(229, 40);
            btnLuuPhienBan.TabIndex = 1;
            btnLuuPhienBan.Text = "Lưu phiên bản mới";
            // 
            // btnKhongLuuPhienBan
            // 
            btnKhongLuuPhienBan.Location = new Point(465, 533);
            btnKhongLuuPhienBan.Margin = new Padding(3, 4, 3, 4);
            btnKhongLuuPhienBan.Name = "btnKhongLuuPhienBan";
            btnKhongLuuPhienBan.Size = new Size(229, 40);
            btnKhongLuuPhienBan.TabIndex = 0;
            btnKhongLuuPhienBan.Text = "Không lưu";
            // 
            // label2
            // 
            label2.AutoSize = true;
            label2.Location = new Point(100, 88);
            label2.Name = "label2";
            label2.Size = new Size(93, 20);
            label2.TabIndex = 7;
            label2.Text = "Tên mô hình:";
            // 
            // label3
            // 
            label3.AutoSize = true;
            label3.Location = new Point(100, 126);
            label3.Name = "label3";
            label3.Size = new Size(218, 20);
            label3.TabIndex = 8;
            label3.Text = "Tên phiên bản được huấn luyện:";
            // 
            // label4
            // 
            label4.AutoSize = true;
            label4.Location = new Point(100, 167);
            label4.Name = "label4";
            label4.Size = new Size(138, 20);
            label4.TabIndex = 9;
            label4.Text = "Kết quả huấn luyện:";
            // 
            // label5
            // 
            label5.AutoSize = true;
            label5.Location = new Point(162, 367);
            label5.Name = "label5";
            label5.Size = new Size(0, 20);
            label5.TabIndex = 10;
            // 
            // lbTenMoHinh
            // 
            lbTenMoHinh.AutoSize = true;
            lbTenMoHinh.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbTenMoHinh.Location = new Point(208, 88);
            lbTenMoHinh.Name = "lbTenMoHinh";
            lbTenMoHinh.Size = new Size(96, 20);
            lbTenMoHinh.TabIndex = 11;
            lbTenMoHinh.Text = "Tên mô hình";
            // 
            // lbTenPhienBanHL
            // 
            lbTenPhienBanHL.AutoSize = true;
            lbTenPhienBanHL.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbTenPhienBanHL.Location = new Point(324, 126);
            lbTenPhienBanHL.Name = "lbTenPhienBanHL";
            lbTenPhienBanHL.Size = new Size(107, 20);
            lbTenPhienBanHL.TabIndex = 12;
            lbTenPhienBanHL.Text = "Ten phiên bản";
            // 
            // lbTrangThai
            // 
            lbTrangThai.AutoSize = true;
            lbTrangThai.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbTrangThai.Location = new Point(274, 206);
            lbTrangThai.Name = "lbTrangThai";
            lbTrangThai.Size = new Size(171, 20);
            lbTrangThai.TabIndex = 13;
            lbTrangThai.Text = "Huấn luyện thành công";
            // 
            // lbDoChinhXac
            // 
            lbDoChinhXac.AutoSize = true;
            lbDoChinhXac.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbDoChinhXac.Location = new Point(286, 246);
            lbDoChinhXac.Name = "lbDoChinhXac";
            lbDoChinhXac.Size = new Size(98, 20);
            lbDoChinhXac.TabIndex = 14;
            lbDoChinhXac.Text = "Độ chính xác";
            // 
            // lbDoNhay
            // 
            lbDoNhay.AutoSize = true;
            lbDoNhay.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbDoNhay.Location = new Point(312, 286);
            lbDoNhay.Name = "lbDoNhay";
            lbDoNhay.Size = new Size(67, 20);
            lbDoNhay.TabIndex = 15;
            lbDoNhay.Text = "Độ nhạy";
            // 
            // lbThoiGianChay
            // 
            lbThoiGianChay.AutoSize = true;
            lbThoiGianChay.Font = new Font("Segoe UI", 9F, FontStyle.Bold, GraphicsUnit.Point);
            lbThoiGianChay.Location = new Point(302, 326);
            lbThoiGianChay.Name = "lbThoiGianChay";
            lbThoiGianChay.Size = new Size(110, 20);
            lbThoiGianChay.TabIndex = 16;
            lbThoiGianChay.Text = "Thời gian chạy";
            // 
            // KetQuaHLFrm
            // 
            AutoScaleDimensions = new SizeF(8F, 20F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(914, 600);
            Controls.Add(lbThoiGianChay);
            Controls.Add(lbDoNhay);
            Controls.Add(lbDoChinhXac);
            Controls.Add(lbTrangThai);
            Controls.Add(lbTenPhienBanHL);
            Controls.Add(lbTenMoHinh);
            Controls.Add(label5);
            Controls.Add(label4);
            Controls.Add(label3);
            Controls.Add(label2);
            Controls.Add(btnKhongLuuPhienBan);
            Controls.Add(btnLuuPhienBan);
            Controls.Add(lbModel);
            Controls.Add(lbTime);
            Controls.Add(lbRecall);
            Controls.Add(lbCorrect);
            Controls.Add(label1);
            Margin = new Padding(3, 4, 3, 4);
            Name = "KetQuaHLFrm";
            Text = "Ứng dụng nhận dạng biển số - Kết quả";
            ResumeLayout(false);
            PerformLayout();
        }

        private Label label1;
        private Label lbCorrect;
        private Label lbRecall;
        private Label lbTime;
        private Label lbModel;
        private Button btnLuuPhienBan;
        private Button btnKhongLuuPhienBan;
        private Label label2;
        private Label label3;
        private Label label4;
        private Label label5;
        private Label lbTenMoHinh;
        private Label lbTenPhienBanHL;
        private Label lbTrangThai;
        private Label lbDoChinhXac;
        private Label lbDoNhay;
        private Label lbThoiGianChay;
    }
}
