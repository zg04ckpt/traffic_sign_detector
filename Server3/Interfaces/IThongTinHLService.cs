using Model.Entities;

namespace Server3.Interfaces
{
    public interface IThongTinHLService
    {
        Task<ThongTinHL> CreateThongTinHLAsync(ThongTinHL thongTinHL);
        Task SaveThongTinHLAsync(ThongTinHL thongTinHL);
        Task<int> CreatePhienBanFromTrainResultAsync(int thongTinHLId);
        Task<ThongTinHL> GetThongTinHLAsync(int thongTinHLId);
    }
}
