namespace Client.Views
{
    partial class CauHinhFrm
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
            inputLR = new TextBox();
            label1 = new Label();
            tableLayoutPanel1 = new TableLayoutPanel();
            cbOptimizer = new ComboBox();
            cbSelectDeviceType = new ComboBox();
            inputESPatience = new TextBox();
            inputImgSize = new TextBox();
            inputEpochs = new TextBox();
            inputBatchSize = new TextBox();
            label3 = new Label();
            label4 = new Label();
            label5 = new Label();
            label6 = new Label();
            label7 = new Label();
            label8 = new Label();
            label9 = new Label();
            btnTiepTuc = new Button();
            tableLayoutPanel1.SuspendLayout();
            SuspendLayout();
            // 
            // inputLR
            // 
            inputLR.Dock = DockStyle.Fill;
            inputLR.ImeMode = ImeMode.NoControl;
            inputLR.Location = new Point(342, 66);
            inputLR.Margin = new Padding(3, 2, 3, 2);
            inputLR.Name = "inputLR";
            inputLR.Size = new Size(334, 23);
            inputLR.TabIndex = 11;
            inputLR.Text = "0.01";
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Location = new Point(10, 7);
            label1.Name = "label1";
            label1.Size = new Size(163, 15);
            label1.TabIndex = 0;
            label1.Text = "Cấu hình tham số huấn luyện";
            // 
            // tableLayoutPanel1
            // 
            tableLayoutPanel1.ColumnCount = 2;
            tableLayoutPanel1.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50F));
            tableLayoutPanel1.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 50F));
            tableLayoutPanel1.Controls.Add(cbOptimizer, 1, 6);
            tableLayoutPanel1.Controls.Add(cbSelectDeviceType, 1, 5);
            tableLayoutPanel1.Controls.Add(inputESPatience, 1, 4);
            tableLayoutPanel1.Controls.Add(inputImgSize, 1, 3);
            tableLayoutPanel1.Controls.Add(inputLR, 1, 2);
            tableLayoutPanel1.Controls.Add(inputEpochs, 1, 1);
            tableLayoutPanel1.Controls.Add(inputBatchSize, 1, 0);
            tableLayoutPanel1.Controls.Add(label3, 0, 0);
            tableLayoutPanel1.Controls.Add(label4, 0, 1);
            tableLayoutPanel1.Controls.Add(label5, 0, 2);
            tableLayoutPanel1.Controls.Add(label6, 0, 3);
            tableLayoutPanel1.Controls.Add(label7, 0, 4);
            tableLayoutPanel1.Controls.Add(label8, 0, 5);
            tableLayoutPanel1.Controls.Add(label9, 0, 6);
            tableLayoutPanel1.Location = new Point(10, 31);
            tableLayoutPanel1.Margin = new Padding(3, 2, 3, 2);
            tableLayoutPanel1.Name = "tableLayoutPanel1";
            tableLayoutPanel1.RowCount = 8;
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.RowStyles.Add(new RowStyle(SizeType.Percent, 12.5F));
            tableLayoutPanel1.Size = new Size(679, 257);
            tableLayoutPanel1.TabIndex = 1;
            // 
            // cbOptimizer
            // 
            cbOptimizer.AllowDrop = true;
            cbOptimizer.Dock = DockStyle.Fill;
            cbOptimizer.FormattingEnabled = true;
            cbOptimizer.Location = new Point(342, 194);
            cbOptimizer.Margin = new Padding(3, 2, 3, 2);
            cbOptimizer.Name = "cbOptimizer";
            cbOptimizer.Size = new Size(334, 23);
            cbOptimizer.TabIndex = 15;
            cbOptimizer.Text = "Adam";
            // 
            // cbSelectDeviceType
            // 
            cbSelectDeviceType.AllowDrop = true;
            cbSelectDeviceType.Dock = DockStyle.Fill;
            cbSelectDeviceType.FormattingEnabled = true;
            cbSelectDeviceType.Location = new Point(342, 162);
            cbSelectDeviceType.Margin = new Padding(3, 2, 3, 2);
            cbSelectDeviceType.Name = "cbSelectDeviceType";
            cbSelectDeviceType.Size = new Size(334, 23);
            cbSelectDeviceType.TabIndex = 14;
            cbSelectDeviceType.Text = "cpu";
            // 
            // inputESPatience
            // 
            inputESPatience.Dock = DockStyle.Fill;
            inputESPatience.Location = new Point(342, 130);
            inputESPatience.Margin = new Padding(3, 2, 3, 2);
            inputESPatience.Name = "inputESPatience";
            inputESPatience.Size = new Size(334, 23);
            inputESPatience.TabIndex = 13;
            inputESPatience.Text = "5";
            // 
            // inputImgSize
            // 
            inputImgSize.Dock = DockStyle.Fill;
            inputImgSize.Location = new Point(342, 98);
            inputImgSize.Margin = new Padding(3, 2, 3, 2);
            inputImgSize.Name = "inputImgSize";
            inputImgSize.Size = new Size(334, 23);
            inputImgSize.TabIndex = 12;
            inputImgSize.Text = "416";
            // 
            // inputEpochs
            // 
            inputEpochs.Dock = DockStyle.Fill;
            inputEpochs.Location = new Point(342, 34);
            inputEpochs.Margin = new Padding(3, 2, 3, 2);
            inputEpochs.Name = "inputEpochs";
            inputEpochs.Size = new Size(334, 23);
            inputEpochs.TabIndex = 10;
            inputEpochs.Text = "2";
            // 
            // inputBatchSize
            // 
            inputBatchSize.Dock = DockStyle.Fill;
            inputBatchSize.Location = new Point(342, 2);
            inputBatchSize.Margin = new Padding(3, 2, 3, 2);
            inputBatchSize.Name = "inputBatchSize";
            inputBatchSize.Size = new Size(334, 23);
            inputBatchSize.TabIndex = 9;
            inputBatchSize.Text = "256";
            // 
            // label3
            // 
            label3.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label3.AutoSize = true;
            label3.Location = new Point(3, 0);
            label3.Name = "label3";
            label3.Size = new Size(333, 32);
            label3.TabIndex = 1;
            label3.Text = "Batch Size";
            label3.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label4
            // 
            label4.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label4.AutoSize = true;
            label4.Location = new Point(3, 32);
            label4.Name = "label4";
            label4.Size = new Size(333, 32);
            label4.TabIndex = 2;
            label4.Text = "Epochs";
            label4.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label5
            // 
            label5.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label5.AutoSize = true;
            label5.Location = new Point(3, 64);
            label5.Name = "label5";
            label5.Size = new Size(333, 32);
            label5.TabIndex = 3;
            label5.Text = "Learning Rate";
            label5.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label6
            // 
            label6.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label6.AutoSize = true;
            label6.Location = new Point(3, 96);
            label6.Name = "label6";
            label6.Size = new Size(333, 32);
            label6.TabIndex = 4;
            label6.Text = "Kích Thước Ảnh";
            label6.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label7
            // 
            label7.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label7.AutoSize = true;
            label7.Location = new Point(3, 128);
            label7.Name = "label7";
            label7.Size = new Size(333, 32);
            label7.TabIndex = 5;
            label7.Text = "Early Stopping Patience";
            label7.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label8
            // 
            label8.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label8.AutoSize = true;
            label8.Location = new Point(3, 160);
            label8.Name = "label8";
            label8.Size = new Size(333, 32);
            label8.TabIndex = 6;
            label8.Text = "Loại thiết bị";
            label8.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // label9
            // 
            label9.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            label9.AutoSize = true;
            label9.Location = new Point(3, 192);
            label9.Name = "label9";
            label9.Size = new Size(333, 32);
            label9.TabIndex = 7;
            label9.Text = "Optimizer";
            label9.TextAlign = ContentAlignment.MiddleLeft;
            // 
            // btnTiepTuc
            // 
            btnTiepTuc.Location = new Point(249, 305);
            btnTiepTuc.Margin = new Padding(3, 2, 3, 2);
            btnTiepTuc.Name = "btnTiepTuc";
            btnTiepTuc.Size = new Size(200, 22);
            btnTiepTuc.TabIndex = 2;
            btnTiepTuc.Text = "Huấn Luyện";
            btnTiepTuc.UseVisualStyleBackColor = true;
            // 
            // CauHinhFrm
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(700, 338);
            Controls.Add(btnTiepTuc);
            Controls.Add(tableLayoutPanel1);
            Controls.Add(label1);
            Margin = new Padding(3, 2, 3, 2);
            Name = "CauHinhFrm";
            Text = "Ứng dụng nhận dạng biển số - Cấu hình";
            tableLayoutPanel1.ResumeLayout(false);
            tableLayoutPanel1.PerformLayout();
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Label label1;
        private TableLayoutPanel tableLayoutPanel1;
        private Label label3;
        private Label label4;
        private Label label5;
        private Label label6;
        private Label label7;
        private Label label8;
        private Label label9;
        private Button btnTiepTuc;
        private ComboBox comboBox1;
        private ComboBox cbOptimizer;
        private ComboBox cbSelectDeviceType;
        private TextBox inputESPatience;
        private TextBox inputImgSize;
        private TextBox inputLR;
        private TextBox textBox2;
        private TextBox textBox1;
        private TextBox inputEpochs;
        private TextBox inputBatchSize;
    }
}