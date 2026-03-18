using Model.Entities;

namespace Server3.Interfaces
{
    public interface ITapDuLieuService
    {
        Task<List<TapDuLieu>> GetAllTapDuLieuAsync();
        Task<List<Mau>> GetAllMauAsync(List<int> mauIds);
    }
}
