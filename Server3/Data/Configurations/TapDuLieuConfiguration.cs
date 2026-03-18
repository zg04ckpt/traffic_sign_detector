using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class TapDuLieuConfiguration : IEntityTypeConfiguration<TapDuLieu>
    {
        public void Configure(EntityTypeBuilder<TapDuLieu> builder)
        {
            builder.ToTable("TapDuLieu");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.Property(x => x.Ten)
                .IsRequired()
                .HasMaxLength(200);

            builder.HasMany(x => x.DsMau)
                .WithOne()
                .HasForeignKey("TapDuLieuId")
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
