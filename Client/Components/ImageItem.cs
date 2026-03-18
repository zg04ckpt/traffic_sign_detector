using Model.Entities;
using System.Threading.Tasks;

namespace Client.Components
{
    public partial class ImageItem : UserControl
    {
        public event Action<Mau, bool>? OnImageSelectedChanged;
        private readonly Mau mau;

        public ImageItem(Mau mau)
        {
            this.mau = mau;

            InitializeComponent();
            Margin = new Padding(0, 0, 10, 10);
            Padding = new Padding(0);
            Size = new Size(100, 100);

            img.BackColor = Color.Black;
            img.Dock = DockStyle.Fill;
            img.SizeMode = PictureBoxSizeMode.Zoom;
            img.Margin = new Padding(0);
            img.LoadAsync(mau.DuongDanAnh);
            cbChon.BackColor = Color.Transparent;
            cbChon.FlatStyle = FlatStyle.Flat;
            cbChon.FlatAppearance.BorderSize = 0;
            cbChon.Parent = img;
            cbChon.BringToFront();
            cbChon.CheckedChanged += CbChon_CheckedChanged;

            panel1.Dock = DockStyle.Fill;
            panel1.Margin = new Padding(0);
            panel1.Padding = new Padding(0);
        }

        private void CbChon_CheckedChanged(object? sender, EventArgs e)
        {
            OnImageSelectedChanged?.Invoke(mau, cbChon.Checked);
            if (cbChon.Checked)
            {
                panel1.BackColor = Color.BlueViolet;
                panel1.Padding = new Padding(3);
            }
            else
            {
                panel1.BackColor = Color.Transparent;
                panel1.Padding = new Padding(0);
            }
        }
    }
}
