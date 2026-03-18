using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Server3.Data.Migrations
{
    public partial class AddTapDuLieuFK : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "TapDuLieuId",
                table: "Mau",
                type: "integer",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Mau_TapDuLieuId",
                table: "Mau",
                column: "TapDuLieuId");

            migrationBuilder.AddForeignKey(
                name: "FK_Mau_TapDuLieu_TapDuLieuId",
                table: "Mau",
                column: "TapDuLieuId",
                principalTable: "TapDuLieu",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_Mau_TapDuLieu_TapDuLieuId",
                table: "Mau");

            migrationBuilder.DropIndex(
                name: "IX_Mau_TapDuLieuId",
                table: "Mau");

            migrationBuilder.DropColumn(
                name: "TapDuLieuId",
                table: "Mau");
        }
    }
}
