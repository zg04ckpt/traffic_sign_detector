using Microsoft.Extensions.FileProviders;
using Server3.Data;
using Server3.Hubs;
using Server3.Interfaces;
using Server3.Middlewares;
using Server3.Services;

namespace Server3
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services.AddDbService();
            builder.Services.AddTransient<GlobalExceptionCatchingMiddleware>();
            builder.Services.AddScoped<IMoHinhService, MoHinhService>();
            builder.Services.AddScoped<IThongTinHLService, ThongTinHLService>();
            builder.Services.AddScoped<ITapDuLieuService, TapDuLieuService>();
            builder.Services.AddScoped<IHuanLuyenService, HuanLuyenService>();

            builder.Services.AddSignalR();

            builder.Services.AddControllers();
            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            var app = builder.Build();
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();
                app.UseSwaggerUI();
            }
            app.UseMiddleware<GlobalExceptionCatchingMiddleware>();

            app.UseStaticFiles(new StaticFileOptions
            {
                FileProvider = new PhysicalFileProvider(
                    Path.Combine(AppContext.BaseDirectory, "uploads")),
                RequestPath = "/uploads"
            });

            app.UseHttpsRedirection();
            app.UseAuthorization();
            app.MapControllers();
            app.MapHub<TrainingStatusHub>("/training-status");
            app.Run();
        }
    }
}
