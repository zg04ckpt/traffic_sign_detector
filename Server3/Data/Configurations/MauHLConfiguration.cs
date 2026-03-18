using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class MauHLConfiguration : IEntityTypeConfiguration<MauHL>
    {
        public void Configure(EntityTypeBuilder<MauHL> builder)
        {
            builder.ToTable("MauHL");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).UseIdentityColumn();

            builder.HasOne(x => x.ThongTin)
                .WithMany()
                .HasForeignKey("MauId")
                .OnDelete(DeleteBehavior.Cascade);
        }
    }
}
