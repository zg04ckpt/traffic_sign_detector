namespace Client.Views
{
    partial class ChonMauFrm
    {
        private System.ComponentModel.IContainer components = null;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null)) components.Dispose();
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            dsMau = new FlowLayoutPanel();
            btnTiepTuc = new Button();
            bottomPanel = new Panel();
            lbSlMauDaChon = new Label();
            cbDsTapDuLieu = new ComboBox();
            panel1 = new Panel();
            bottomPanel.SuspendLayout();
            panel1.SuspendLayout();
            SuspendLayout();
            // 
            // dsMau
            // 
            dsMau.AutoScroll = true;
            dsMau.Dock = DockStyle.Fill;
            dsMau.Location = new Point(0, 23);
            dsMau.Name = "dsMau";
            dsMau.Padding = new Padding(0, 10, 0, 10);
            dsMau.Size = new Size(893, 422);
            dsMau.TabIndex = 1;
            // 
            // btnTiepTuc
            // 
            btnTiepTuc.Font = new Font("Segoe UI", 12F, FontStyle.Regular, GraphicsUnit.Point);
            btnTiepTuc.Location = new Point(368, 0);
            btnTiepTuc.Name = "btnTiepTuc";
            btnTiepTuc.Size = new Size(175, 30);
            btnTiepTuc.TabIndex = 1;
            btnTiepTuc.Text = "Tiếp tục";
            btnTiepTuc.Click += BtnTiepTuc_Click;
            // 
            // bottomPanel
            // 
            bottomPanel.Controls.Add(lbSlMauDaChon);
            bottomPanel.Controls.Add(btnTiepTuc);
            bottomPanel.Dock = DockStyle.Bottom;
            bottomPanel.Location = new Point(0, 445);
            bottomPanel.Name = "bottomPanel";
            bottomPanel.Size = new Size(893, 30);
            bottomPanel.TabIndex = 0;
            // 
            // lbSlMauDaChon
            // 
            lbSlMauDaChon.AutoSize = true;
            lbSlMauDaChon.Location = new Point(3, 8);
            lbSlMauDaChon.Name = "lbSlMauDaChon";
            lbSlMauDaChon.Size = new Size(60, 15);
            lbSlMauDaChon.TabIndex = 0;
            lbSlMauDaChon.Text = "Đã chọn 0";
            // 
            // cbDsTapDuLieu
            // 
            cbDsTapDuLieu.Dock = DockStyle.Top;
            cbDsTapDuLieu.FormattingEnabled = true;
            cbDsTapDuLieu.Location = new Point(0, 0);
            cbDsTapDuLieu.Name = "cbDsTapDuLieu";
            cbDsTapDuLieu.Size = new Size(893, 23);
            cbDsTapDuLieu.TabIndex = 2;
            // 
            // panel1
            // 
            panel1.Controls.Add(dsMau);
            panel1.Controls.Add(bottomPanel);
            panel1.Controls.Add(cbDsTapDuLieu);
            panel1.Dock = DockStyle.Fill;
            panel1.Location = new Point(18, 15);
            panel1.Name = "panel1";
            panel1.Size = new Size(893, 475);
            panel1.TabIndex = 3;
            // 
            // ChonMauFrm
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(929, 505);
            Controls.Add(panel1);
            Name = "ChonMauFrm";
            Padding = new Padding(18, 15, 18, 15);
            Text = "Ứng dụng nhận dạng biển số - Chọn ảnh";
            bottomPanel.ResumeLayout(false);
            bottomPanel.PerformLayout();
            panel1.ResumeLayout(false);
            ResumeLayout(false);
        }

        private FlowLayoutPanel dsMau;
        private Button btnTiepTuc;
        private Panel bottomPanel;
        private Label lbSlMauDaChon;
        private ComboBox cbDsTapDuLieu;
        private Panel panel1;
    }
}
