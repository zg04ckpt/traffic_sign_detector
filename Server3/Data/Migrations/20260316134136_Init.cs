using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace Server3.Data.Migrations
{
    public partial class Init : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "LoaiBien",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    Ten = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_LoaiBien", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "Mau",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    DuongDanAnh = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false),
                    DoPhanGiai = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Mau", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "MoHinh",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    Ten = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    MoHinhGoc = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MoHinh", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "TapDuLieu",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    Ten = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_TapDuLieu", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "KhungNhanDang",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    XCenter = table.Column<float>(type: "real", nullable: false),
                    YCenter = table.Column<float>(type: "real", nullable: false),
                    W = table.Column<float>(type: "real", nullable: false),
                    H = table.Column<float>(type: "real", nullable: false),
                    BienId = table.Column<int>(type: "integer", nullable: false),
                    MauId = table.Column<int>(type: "integer", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_KhungNhanDang", x => x.Id);
                    table.ForeignKey(
                        name: "FK_KhungNhanDang_LoaiBien_BienId",
                        column: x => x.BienId,
                        principalTable: "LoaiBien",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_KhungNhanDang_Mau_MauId",
                        column: x => x.MauId,
                        principalTable: "Mau",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "PhienBan",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    Ten = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    MoTa = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false),
                    DuongDanMH = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false),
                    MoHinhId = table.Column<int>(type: "integer", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_PhienBan", x => x.Id);
                    table.ForeignKey(
                        name: "FK_PhienBan_MoHinh_MoHinhId",
                        column: x => x.MoHinhId,
                        principalTable: "MoHinh",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "ThongTinHL",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    Epochs = table.Column<int>(type: "integer", nullable: false),
                    TrangThai = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    BatDauLuc = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    KetThucLuc = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    DoChinhXac = table.Column<float>(type: "real", nullable: false),
                    DoNhay = table.Column<float>(type: "real", nullable: false),
                    Batch = table.Column<int>(type: "integer", nullable: false),
                    LearningRate = table.Column<float>(type: "real", nullable: false),
                    KichThuocAnh = table.Column<int>(type: "integer", nullable: false),
                    LoaiThietBi = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    EarlyStoppingPatience = table.Column<int>(type: "integer", nullable: false),
                    Optimizer = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    PhienBanHLId = table.Column<int>(type: "integer", nullable: false),
                    MoHinhHLId = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ThongTinHL", x => x.Id);
                    table.ForeignKey(
                        name: "FK_ThongTinHL_MoHinh_MoHinhHLId",
                        column: x => x.MoHinhHLId,
                        principalTable: "MoHinh",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_ThongTinHL_PhienBan_PhienBanHLId",
                        column: x => x.PhienBanHLId,
                        principalTable: "PhienBan",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "MauHL",
                columns: table => new
                {
                    Id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    MauId = table.Column<int>(type: "integer", nullable: false),
                    ThongTinHLId = table.Column<int>(type: "integer", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_MauHL", x => x.Id);
                    table.ForeignKey(
                        name: "FK_MauHL_Mau_MauId",
                        column: x => x.MauId,
                        principalTable: "Mau",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_MauHL_ThongTinHL_ThongTinHLId",
                        column: x => x.ThongTinHLId,
                        principalTable: "ThongTinHL",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_KhungNhanDang_BienId",
                table: "KhungNhanDang",
                column: "BienId");

            migrationBuilder.CreateIndex(
                name: "IX_KhungNhanDang_MauId",
                table: "KhungNhanDang",
                column: "MauId");

            migrationBuilder.CreateIndex(
                name: "IX_MauHL_MauId",
                table: "MauHL",
                column: "MauId");

            migrationBuilder.CreateIndex(
                name: "IX_MauHL_ThongTinHLId",
                table: "MauHL",
                column: "ThongTinHLId");

            migrationBuilder.CreateIndex(
                name: "IX_PhienBan_MoHinhId",
                table: "PhienBan",
                column: "MoHinhId");

            migrationBuilder.CreateIndex(
                name: "IX_ThongTinHL_MoHinhHLId",
                table: "ThongTinHL",
                column: "MoHinhHLId");

            migrationBuilder.CreateIndex(
                name: "IX_ThongTinHL_PhienBanHLId",
                table: "ThongTinHL",
                column: "PhienBanHLId");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "KhungNhanDang");

            migrationBuilder.DropTable(
                name: "MauHL");

            migrationBuilder.DropTable(
                name: "TapDuLieu");

            migrationBuilder.DropTable(
                name: "LoaiBien");

            migrationBuilder.DropTable(
                name: "Mau");

            migrationBuilder.DropTable(
                name: "ThongTinHL");

            migrationBuilder.DropTable(
                name: "PhienBan");

            migrationBuilder.DropTable(
                name: "MoHinh");
        }
    }
}
