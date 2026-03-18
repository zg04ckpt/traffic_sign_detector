using Model.Entities;

namespace Server3.Interfaces
{
    public interface IMoHinhService
    {
        Task<List<MoHinh>> GetAllMoHinhAsync();
        Task<MoHinh> GetMoHinhAsync(int moHinhId);
        Task<PhienBan> GetPhienBanAsync(int phienBanId);
        Task<int> SaveNewPhienBanAsync(MoHinh mohinh, PhienBan phienBan);
    }
}
