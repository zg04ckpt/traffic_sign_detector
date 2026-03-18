using Client.Views;

namespace Client
{
    public class App : ApplicationContext
    {
        public App()
        {
            var quanLyFrm = new QuanLyFrm();
            quanLyFrm.Show();
        }
    }
}
