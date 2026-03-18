using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Server3.Data.Migrations
{
    public partial class AddBatchSize : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "BatchSize",
                table: "ThongTinHL",
                type: "integer",
                nullable: false,
                defaultValue: 0);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "BatchSize",
                table: "ThongTinHL");
        }
    }
}
