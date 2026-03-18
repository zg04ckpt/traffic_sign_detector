namespace Client.Components
{
    public partial class LoadingDialog : Form
    {
        public LoadingDialog(string message = "Đang tải dữ liệu...")
        {
            InitializeComponent();
            StartPosition = FormStartPosition.CenterScreen;
            FormBorderStyle = FormBorderStyle.None;
            BackColor = Color.White;
            Size = new Size(300, 50);
            Padding = new Padding(8);

            var label = new Label
            {
                Text = message,
                AutoSize = false,
                TextAlign = ContentAlignment.MiddleCenter,
                Dock = DockStyle.Fill,
                Font = new Font("Segoe UI", 8F, FontStyle.Regular)
            };

            var progressBar = new ProgressBar
            {
                Style = ProgressBarStyle.Marquee,
                MarqueeAnimationSpeed = 30,
                Dock = DockStyle.Bottom,
                Height = 10
            };

            Controls.Add(label);
            Controls.Add(progressBar);
        }
    }
}
