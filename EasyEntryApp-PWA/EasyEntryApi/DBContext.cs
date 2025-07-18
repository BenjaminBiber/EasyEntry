using doorOpener.Models;
using Microsoft.EntityFrameworkCore;

namespace EasyEntryApi;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) {}

    public DbSet<Device> Devices => Set<Device>();
    public DbSet<Setting> Settings => Set<Setting>();
    public DbSet<DeviceGroup> DeviceGroups => Set<DeviceGroup>();
    public DbSet<Routine> Routines => Set<Routine>();
    public DbSet<RoutineStep> RoutineSteps => Set<RoutineStep>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<DeviceGroup>()
            .HasMany(g => g.Devices)
            .WithOne(d => d.DeviceGroup!)
            .HasForeignKey(d => d.DeviceGroupId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<Routine>()
            .HasMany(r => r.Steps)
            .WithOne(s => s.Routine!)
            .HasForeignKey(s => s.RoutineId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
