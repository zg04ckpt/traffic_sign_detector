namespace Client.Views
{
    partial class ChonMoHinhFrm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            label1 = new Label();
            cbDsMoHinh = new ComboBox();
            label2 = new Label();
            cbDsPhienBan = new ComboBox();
            btnTiepTuc = new Button();
            SuspendLayout();
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Location = new Point(109, 41);
            label1.Name = "label1";
            label1.Size = new Size(84, 15);
            label1.TabIndex = 0;
            label1.Text = "Chọn mô hình";
            label1.UseMnemonic = false;
            // 
            // cbDsMoHinh
            // 
            cbDsMoHinh.FormattingEnabled = true;
            cbDsMoHinh.Location = new Point(109, 66);
            cbDsMoHinh.Margin = new Padding(3, 2, 3, 2);
            cbDsMoHinh.Name = "cbDsMoHinh";
            cbDsMoHinh.RightToLeft = RightToLeft.No;
            cbDsMoHinh.Size = new Size(491, 23);
            cbDsMoHinh.TabIndex = 1;
            // 
            // label2
            // 
            label2.AutoSize = true;
            label2.Location = new Point(112, 131);
            label2.Name = "label2";
            label2.Size = new Size(92, 15);
            label2.TabIndex = 2;
            label2.Text = "Chọn phiên bản";
            // 
            // cbDsPhienBan
            // 
            cbDsPhienBan.FormattingEnabled = true;
            cbDsPhienBan.Location = new Point(112, 157);
            cbDsPhienBan.Margin = new Padding(3, 2, 3, 2);
            cbDsPhienBan.Name = "cbDsPhienBan";
            cbDsPhienBan.Size = new Size(489, 23);
            cbDsPhienBan.TabIndex = 3;
            // 
            // btnTiepTuc
            // 
            btnTiepTuc.Location = new Point(307, 284);
            btnTiepTuc.Margin = new Padding(3, 2, 3, 2);
            btnTiepTuc.Name = "btnTiepTuc";
            btnTiepTuc.Size = new Size(82, 22);
            btnTiepTuc.TabIndex = 4;
            btnTiepTuc.Text = "Tiếp Tục";
            btnTiepTuc.UseVisualStyleBackColor = true;
            // 
            // ChonMoHinhFrm
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(700, 338);
            Controls.Add(btnTiepTuc);
            Controls.Add(cbDsPhienBan);
            Controls.Add(label2);
            Controls.Add(cbDsMoHinh);
            Controls.Add(label1);
            Margin = new Padding(3, 2, 3, 2);
            Name = "ChonMoHinhFrm";
            Text = "Ứng dụng nhận dạng biển số - Chọn mô hình";
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Label label1;
        private ComboBox cbDsMoHinh;
        private Label label2;
        private ComboBox cbDsPhienBan;
        private Button btnTiepTuc;
    }
}