using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class ThongTinHLConfiguration : IEntityTypeConfiguration<ThongTinHL>
    {
        public void Configure(EntityTypeBuilder<ThongTinHL> builder)
        {
            builder.ToTable("ThongTinHL");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.TrangThai).HasMaxLength(100);
            builder.Property(x => x.LoaiThietBi).HasMaxLength(50);
            builder.Property(x => x.Optimizer).HasMaxLength(50);
            builder.Property(x => x.DoChinhXac).HasColumnType("real");
            builder.Property(x => x.DoNhay).HasColumnType("real");
            builder.Property(x => x.LearningRate).HasColumnType("real");

            builder.HasOne(x => x.PhienBanHL)
                .WithMany()
                .HasForeignKey("PhienBanHLId")
                .OnDelete(DeleteBehavior.Cascade);

            builder.HasOne(x => x.MoHinhHL)
                .WithMany()
                .HasForeignKey("MoHinhHLId")
                .OnDelete(DeleteBehavior.Cascade);

            builder.HasMany(x => x.DsMauHL)
                .WithOne()
                .HasForeignKey("ThongTinHLId")
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
