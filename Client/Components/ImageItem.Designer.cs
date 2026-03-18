namespace Client.Components
{
    partial class ImageItem
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            panel1 = new Panel();
            cbChon = new CheckBox();
            img = new PictureBox();
            panel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)img).BeginInit();
            SuspendLayout();
            // 
            // panel1
            // 
            panel1.Controls.Add(cbChon);
            panel1.Controls.Add(img);
            panel1.Location = new Point(0, 0);
            panel1.Margin = new Padding(3, 2, 3, 2);
            panel1.Name = "panel1";
            panel1.Size = new Size(131, 112);
            panel1.TabIndex = 0;
            // 
            // cbChon
            // 
            cbChon.AutoSize = true;
            cbChon.BackColor = Color.Transparent;
            cbChon.BackgroundImageLayout = ImageLayout.None;
            cbChon.Location = new Point(4, 4);
            cbChon.Margin = new Padding(0);
            cbChon.Name = "cbChon";
            cbChon.Size = new Size(15, 14);
            cbChon.TabIndex = 1;
            cbChon.UseVisualStyleBackColor = false;
            // 
            // img
            // 
            img.Location = new Point(0, 0);
            img.Margin = new Padding(3, 2, 3, 2);
            img.Name = "img";
            img.Size = new Size(131, 112);
            img.TabIndex = 0;
            img.TabStop = false;
            // 
            // ImageItem
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            Controls.Add(panel1);
            Margin = new Padding(3, 2, 3, 2);
            Name = "ImageItem";
            Size = new Size(131, 112);
            panel1.ResumeLayout(false);
            panel1.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)img).EndInit();
            ResumeLayout(false);
        }

        #endregion

        private Panel panel1;
        private CheckBox cbChon;
        private PictureBox img;
    }
}
