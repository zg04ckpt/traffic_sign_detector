using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class MauConfiguration : IEntityTypeConfiguration<Mau>
    {
        public void Configure(EntityTypeBuilder<Mau> builder)
        {
            builder.ToTable("Mau");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.DuongDanAnh)
                .IsRequired()
                .HasMaxLength(500);

            builder.Property(x => x.DoPhanGiai)
                .HasMaxLength(50);

            builder.HasMany(x => x.DsBien)
                .WithOne()
                .HasForeignKey("MauId")
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
