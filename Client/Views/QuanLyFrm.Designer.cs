namespace Client.Views
{
    partial class QuanLyFrm
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
            btnHuanLuyenNhanDangVung = new Button();
            SuspendLayout();
            // 
            // btnHuanLuyenNhanDangVung
            // 
            btnHuanLuyenNhanDangVung.Location = new Point(218, 224);
            btnHuanLuyenNhanDangVung.Name = "btnHuanLuyenNhanDangVung";
            btnHuanLuyenNhanDangVung.Size = new Size(357, 29);
            btnHuanLuyenNhanDangVung.TabIndex = 0;
            btnHuanLuyenNhanDangVung.Text = "Huấn Luyện Nhận Dạng Vùng";
            btnHuanLuyenNhanDangVung.UseVisualStyleBackColor = true;
            btnHuanLuyenNhanDangVung.Click += btnHuanLuyenNhanDangVung_Click;
            // 
            // QuanLyFrm
            // 
            AutoScaleDimensions = new SizeF(8F, 20F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(800, 450);
            Controls.Add(btnHuanLuyenNhanDangVung);
            Name = "QuanLyFrm";
            Text = "Ứng dụng nhận dạng biển số - Quản lý";
            ResumeLayout(false);
        }

        #endregion

        private Button btnHuanLuyenNhanDangVung;
    }
}