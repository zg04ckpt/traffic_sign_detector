using Microsoft.EntityFrameworkCore;
using Model.Entities;
using Server3.Interfaces;

namespace Server3.Services
{
    public class MoHinhService : IMoHinhService
    {
        private readonly IRepository<MoHinh> _moHinhrepo;
        private readonly IRepository<PhienBan> _phienBanrepo;

        public MoHinhService(
            IRepository<MoHinh> moHinhrepo, 
            IRepository<PhienBan> phienBanrepo)
        {
            _moHinhrepo = moHinhrepo;
            _phienBanrepo = phienBanrepo;
        }

        public async Task<List<MoHinh>> GetAllMoHinhAsync()
        {
            return await _moHinhrepo.GetQuery()
               .Include(e => e.DsPhienBan)
               .ToListAsync();
        }

        public async Task<MoHinh> GetMoHinhAsync(int moHinhId)
        {
            return await _moHinhrepo.GetFirstAsync(moHinhId);
        }

        public async Task<PhienBan> GetPhienBanAsync(int phienBanId)
        {
            return await _phienBanrepo.GetFirstAsync(phienBanId);
        }

        public async Task<int> SaveNewPhienBanAsync(MoHinh moHinh, PhienBan phienBan)
        {
            moHinh.DsPhienBan.Add(phienBan);

            await _moHinhrepo.UpdateAsync(moHinh);
            await _moHinhrepo.SaveChangesAsync();

            return phienBan.Id;
        }
    }
}
