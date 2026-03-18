using Microsoft.EntityFrameworkCore;
using Model.Entities;
using Server3.Interfaces;

namespace Server3.Services
{
    public class TapDuLieuService : ITapDuLieuService
    {
        private readonly IRepository<TapDuLieu> _repo;
        private readonly IRepository<Mau> mauRepo;

        public TapDuLieuService(
            IRepository<TapDuLieu> repo, 
            IRepository<Mau> mauRepo)
        {
            _repo = repo;
            this.mauRepo = mauRepo;
        }

        public async Task<List<Mau>> GetAllMauAsync(List<int> mauIds)
        {
            return await mauRepo.GetQuery()
                .Where(m => mauIds.Contains(m.Id))
                .ToListAsync();
        }

        public async Task<List<TapDuLieu>> GetAllTapDuLieuAsync()
        {
            var data = await _repo.GetQuery()
                .Include(t => t.DsMau)
                .ToListAsync();
            return data;
        }
    }
}
