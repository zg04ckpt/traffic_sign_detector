using Microsoft.EntityFrameworkCore;
using Model.Entities;
using Server3.Interfaces;

namespace Server3.Services
{
    public class ThongTinHLService : IThongTinHLService
    {
        private readonly IRepository<ThongTinHL> thongTinHLRepo;
        private readonly IMoHinhService moHinhService;
        private readonly ITapDuLieuService tapDuLieuService;

        public ThongTinHLService(
            IRepository<ThongTinHL> thongTinHLRepo,
            IMoHinhService moHinhService,
            ITapDuLieuService tapDuLieuService)
        {
            this.thongTinHLRepo = thongTinHLRepo;
            this.moHinhService = moHinhService;
            this.tapDuLieuService = tapDuLieuService;
        }

        public async Task<ThongTinHL> CreateThongTinHLAsync(ThongTinHL thongTinHL)
        {
            MoHinh moHinh = await moHinhService.GetMoHinhAsync(thongTinHL.MoHinhHL.Id);
            PhienBan phienBan = await moHinhService.GetPhienBanAsync(thongTinHL.PhienBanHL.Id);
            
            // Lấy DS mẫu
            List<int> ids = thongTinHL.DsMauHL.Select(m => m.ThongTin.Id).ToList();
            List<Mau> dsMau = await tapDuLieuService.GetAllMauAsync(ids);
            List<MauHL> dsMauHL = dsMau.Select(m => new MauHL
            {
                ThongTin = m
            }).ToList();
                
            ThongTinHL entity = new()
            {
                Epochs = thongTinHL.Epochs,
                BatchSize = thongTinHL.BatchSize,
                LearningRate = thongTinHL.LearningRate,
                EarlyStoppingPatience = thongTinHL.EarlyStoppingPatience,
                KichThuocAnh = thongTinHL.KichThuocAnh,
                LoaiThietBi = thongTinHL.LoaiThietBi,
                DsMauHL = dsMauHL,
                MoHinhHL = moHinh,
                PhienBanHL = phienBan,
                Optimizer = thongTinHL.Optimizer,
                TrangThai = "Đã khởi tạo cấu hình"
            };

            await thongTinHLRepo.CreateAsync(entity);
            await thongTinHLRepo.SaveChangesAsync();

            // Bắt đầu huấn luyện
            //huanLuyenService.StartTrainMoHinhAsync(thongTinHL);

            return entity;
        }

        public async Task<ThongTinHL> GetThongTinHLAsync(int thongTinHLId)
        {
            return await thongTinHLRepo.GetQuery()
                .Include(t => t.PhienBanHL)
                .Include(t => t.MoHinhHL)
                .Include(t => t.DsMauHL)
                    .ThenInclude(m => m.ThongTin)
                        .ThenInclude(m => m.DsBien)
                            .ThenInclude(k => k.Bien)
                .FirstOrDefaultAsync(e => e.Id == thongTinHLId)
                ?? throw new Exception("Thông tin huấn luyện không tồn tại");
        }

        public async Task<int> CreatePhienBanFromTrainResultAsync(int thongTinHLId)
        {
            var thongTinHL = await thongTinHLRepo.GetQuery()
                .Include(t => t.MoHinhHL)
                    .ThenInclude(m => m.DsPhienBan)
                .FirstOrDefaultAsync(t => t.Id == thongTinHLId)
                ?? throw new Exception("Phiên bản không tồn tại");

            var moHinh = thongTinHL.MoHinhHL;

            if (thongTinHL.TrangThai != "Hoàn thành huấn luyện")
            {
                throw new Exception("Phiên huấn luyện chưa thành công.");
            }

            

            var phienBanMoi = new PhienBan
            {
                MoTa = "Phiên bản từ mô hình " + moHinh.Ten,
                Ten = $"v{moHinh.DsPhienBan.Count}"
            };

            // Copy result sang folder mới
            var srcPath = Path.Combine(
                AppContext.BaseDirectory,
                thongTinHL.DuongDanMoHinhKetQua!);
            var relaPath = Path.Combine("models", thongTinHL.MoHinhHL.Id + "", phienBanMoi.Ten + Path.GetExtension(thongTinHL.DuongDanMoHinhKetQua));
            var destPath = Path.Combine(
                AppContext.BaseDirectory,
                relaPath);
            File.Copy(srcPath, destPath);
            phienBanMoi.DuongDanMH = relaPath;

            int phienBanMoiId = await moHinhService.SaveNewPhienBanAsync(moHinh, phienBanMoi);
            return phienBanMoiId;
        }

        public async Task SaveThongTinHLAsync(ThongTinHL thongTinHL)
        {
            await thongTinHLRepo.UpdateAsync(thongTinHL);
            await thongTinHLRepo.SaveChangesAsync();
        }
    }
}
