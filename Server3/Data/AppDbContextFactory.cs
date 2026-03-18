using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Server3.Data;

namespace Server3.Data
{
    public class AppDbContextFactory : IDesignTimeDbContextFactory<AppDbContext>
    {
        public AppDbContext CreateDbContext(string[] args)
        {
            var optionBuilders = new DbContextOptionsBuilder<AppDbContext>();
            optionBuilders.UseNpgsql("Host=localhost;Database=traffic_sign_detector;Username=postgres;Password=admin");
            return new AppDbContext(optionBuilders.Options);
        }
    }
}
