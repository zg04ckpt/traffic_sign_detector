using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class PhienBanConfiguration : IEntityTypeConfiguration<PhienBan>
    {
        public void Configure(EntityTypeBuilder<PhienBan> builder)
        {
            builder.ToTable("PhienBan");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.Ten)
                .IsRequired()
                .HasMaxLength(100);

            builder.Property(x => x.MoTa)
                .HasMaxLength(500);

            builder.Property(x => x.DuongDanMH)
                .HasMaxLength(500);
        }
    }
}
