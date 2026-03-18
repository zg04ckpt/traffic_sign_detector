using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class MoHinhConfiguration : IEntityTypeConfiguration<MoHinh>
    {
        public void Configure(EntityTypeBuilder<MoHinh> builder)
        {
            builder.ToTable("MoHinh");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.Ten)
                .IsRequired()
                .HasMaxLength(200);

            builder.Property(x => x.MoHinhGoc)
                .HasMaxLength(500);

            builder.HasMany(x => x.DsPhienBan)
                .WithOne()
                .HasForeignKey("MoHinhId")
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
