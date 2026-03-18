using Microsoft.EntityFrameworkCore;
using Server3.Interfaces;

namespace Server3.Data
{
    public static class DI
    {
        public static IServiceCollection AddDbService(this IServiceCollection services)
        {
            services.AddDbContext<AppDbContext>(opt =>
            {
                opt.UseNpgsql("Host=localhost;Database=traffic_sign_detector;Username=postgres;Password=admin");
            });
            services.AddScoped(typeof(IRepository<>), typeof(Repository<>));

            return services;
        }
    }
}
