using Presentation.Common.Models;
using Microsoft.EntityFrameworkCore;

namespace Presentation.Common.Contexts;

public class DatabaseContext : DbContext
{
    public DbSet<DeviceModel> Devices { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder option)
    {
        option.UseSqlite("Data Source = sqlite3.db");
        option.UseQueryTrackingBehavior(QueryTrackingBehavior.NoTracking);
    }
    protected override void OnModelCreating(ModelBuilder modelBuilder)
        => modelBuilder.Entity<DeviceModel>(entity =>
            entity.HasQueryFilter(x => x.Status != StatusDevice.Deleted));
}
