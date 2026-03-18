using Microsoft.AspNetCore.Mvc;
using Model.Entities;
using Server3.Interfaces;

namespace Server3.Controllers
{
    [Route("api")]
    [ApiController]
    public class ServerController : ControllerBase
    {
        private readonly IMoHinhService moHinhService;
        private readonly ITapDuLieuService tapDuLieuService;
        private readonly IThongTinHLService thongTinHLService;
        private readonly IHuanLuyenService huanLuyenService;
        private readonly IServiceScopeFactory scopeFactory;

        public ServerController(
            IMoHinhService moHinhService,
            ITapDuLieuService tapDuLieuService,
            IThongTinHLService thongTinHLService,
            IHuanLuyenService huanLuyenService, 
            IServiceScopeFactory scopeFactory)
        {
            this.moHinhService = moHinhService;
            this.tapDuLieuService = tapDuLieuService;
            this.thongTinHLService = thongTinHLService;
            this.huanLuyenService = huanLuyenService;
            this.scopeFactory = scopeFactory;
        }

        [HttpGet("mo-hinh")]
        public async Task<IActionResult> GetAllMoHinhAsync()
        {
            var data = await moHinhService.GetAllMoHinhAsync();
            return Ok(data);
        }

        [HttpGet("tap-du-lieu")]
        public async Task<IActionResult> GetAllTapDuLieuAsync()
        {
            var data = await tapDuLieuService.GetAllTapDuLieuAsync();
            return Ok(data);
        }

        [HttpPost("huan-luyen")]
        public async Task<IActionResult> CreateThongTinHuanLuyen([FromBody] ThongTinHL thongTinHL)
        {
            var data = await thongTinHLService.CreateThongTinHLAsync(thongTinHL);
            return Ok(data);
        }

        [HttpPost("huan-luyen/{thongTinHLId}/bat-dau")]
        public async Task<IActionResult> StartTrainingAsync(int thongTinHLId)
        {
            _ = Task.Run(async () =>
            {
                try
                {
                    using var scope = scopeFactory.CreateScope();
                    var service = scope.ServiceProvider.GetRequiredService<IHuanLuyenService>();
                    await service.StartTrainMoHinhAsync(thongTinHLId);
                }
                catch (Exception ex)
                {
                    Console.WriteLine("ERROR BACKGROUND: " + ex.ToString());
                }
            });
            return Ok();
        }

        [HttpGet("huan-luyen/{thongTinHLId}/ket-qua")]
        public async Task<IActionResult> GetTrainingResultAsync(int thongTinHLId)
        {
            return Ok(await thongTinHLService.GetThongTinHLAsync(thongTinHLId));
        }

        [HttpPost("huan-luyen/{thongTinHLId}/luu-phien-ban")]
        public async Task<IActionResult> SavePhienBanAsync(int thongTinHLId)
        {
            return Ok(await thongTinHLService.CreatePhienBanFromTrainResultAsync(thongTinHLId));
        }
    }
}
