using Model.Entities;

namespace Server3.Interfaces
{
    public interface IHuanLuyenService
    {
        Task StartTrainMoHinhAsync(int thongTinHLId);
    }
}
