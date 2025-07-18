using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace EasyEntryApi.Migrations;

/// <inheritdoc />
public partial class AddRoutineTables : Migration
{
    /// <inheritdoc />
    protected override void Up(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.CreateTable(
            name: "Routines",
            columns: table => new
            {
                Id = table.Column<int>(type: "int", nullable: false)
                    .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                Name = table.Column<string>(type: "longtext", nullable: false)
                    .Annotation("MySql:CharSet", "utf8mb4")
            },
            constraints: table =>
            {
                table.PrimaryKey("PK_Routines", x => x.Id);
            })
            .Annotation("MySql:CharSet", "utf8mb4");

        migrationBuilder.CreateTable(
            name: "RoutineSteps",
            columns: table => new
            {
                Id = table.Column<int>(type: "int", nullable: false)
                    .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                Order = table.Column<int>(type: "int", nullable: false),
                StepType = table.Column<int>(type: "int", nullable: false),
                DeviceId = table.Column<int>(type: "int", nullable: true),
                Action = table.Column<int>(type: "int", nullable: true),
                DurationSeconds = table.Column<int>(type: "int", nullable: false),
                RoutineId = table.Column<int>(type: "int", nullable: false)
            },
            constraints: table =>
            {
                table.PrimaryKey("PK_RoutineSteps", x => x.Id);
                table.ForeignKey(
                    name: "FK_RoutineSteps_Devices_DeviceId",
                    column: x => x.DeviceId,
                    principalTable: "Devices",
                    principalColumn: "Id");
                table.ForeignKey(
                    name: "FK_RoutineSteps_Routines_RoutineId",
                    column: x => x.RoutineId,
                    principalTable: "Routines",
                    principalColumn: "Id",
                    onDelete: ReferentialAction.Cascade);
            })
            .Annotation("MySql:CharSet", "utf8mb4");

        migrationBuilder.CreateIndex(
            name: "IX_RoutineSteps_DeviceId",
            table: "RoutineSteps",
            column: "DeviceId");

        migrationBuilder.CreateIndex(
            name: "IX_RoutineSteps_RoutineId",
            table: "RoutineSteps",
            column: "RoutineId");
    }

    /// <inheritdoc />
    protected override void Down(MigrationBuilder migrationBuilder)
    {
        migrationBuilder.DropTable(
            name: "RoutineSteps");

        migrationBuilder.DropTable(
            name: "Routines");
    }
}

