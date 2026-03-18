using Client.Components;

namespace Client.Views
{
    public partial class BaseFrm : Form
    {
        private LoadingDialog? _loading;

        protected BaseFrm()
        {
            
        }

        protected void ShowLoading(string? message = null)
        {
            if (string.IsNullOrEmpty(message))
            {
                _loading = new LoadingDialog();
            }
            else
            {
                _loading = new LoadingDialog(message);
            }

            _loading.Show();
        }

        protected void HideLoading()
        {
            _loading?.Close();
            _loading = null;
        }
    }
}
