using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class KhungNhanDangConfiguration : IEntityTypeConfiguration<KhungNhanDang>
    {
        public void Configure(EntityTypeBuilder<KhungNhanDang> builder)
        {
            builder.ToTable("KhungNhanDang");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.XCenter).HasColumnType("real");
            builder.Property(x => x.YCenter).HasColumnType("real");
            builder.Property(x => x.W).HasColumnType("real");
            builder.Property(x => x.H).HasColumnType("real");

            builder.HasOne(x => x.Bien)
                .WithMany()
                .HasForeignKey("BienId")
                .OnDelete(DeleteBehavior.Restrict);
        }
    }
}
