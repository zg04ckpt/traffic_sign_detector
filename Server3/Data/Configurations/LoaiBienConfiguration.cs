using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Model.Entities;

namespace Server3.Data.Configurations
{
    public class LoaiBienConfiguration : IEntityTypeConfiguration<LoaiBien>
    {
        public void Configure(EntityTypeBuilder<LoaiBien> builder)
        {
            builder.ToTable("LoaiBien");

            builder.HasKey(x => x.Id);
            builder.Property(x => x.Id).ValueGeneratedNever();

            builder.Property(x => x.Ten)
                .IsRequired()
                .HasMaxLength(200);
        }
    }
}
