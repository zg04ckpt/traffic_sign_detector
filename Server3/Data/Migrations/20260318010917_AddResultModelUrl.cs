using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Server3.Data.Migrations
{
    public partial class AddResultModelUrl : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "DuongDanMoHinhKetQua",
                table: "ThongTinHL",
                type: "text",
                nullable: true);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "DuongDanMoHinhKetQua",
                table: "ThongTinHL");
        }
    }
}
