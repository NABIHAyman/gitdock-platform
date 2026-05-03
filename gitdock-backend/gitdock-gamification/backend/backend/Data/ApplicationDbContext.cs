using Microsoft.EntityFrameworkCore;
using backend.Domain;

namespace backend.Data;
public class ApplicationDbContext : DbContext
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options) { }
    public DbSet<Badge> Badges { get; set; }
    public DbSet<Level> Levels { get; set; }
    public DbSet<Tag> Tags { get; set; }
    public DbSet<LevelTagRequirement> LevelTagRequirements { get; set; }
    public DbSet<XpConfig> XpConfigs { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Badge>().Property(b => b.Type).HasConversion<string>();
        modelBuilder.Entity<Badge>().HasQueryFilter(b => b.DeletedAt == null);

        // --- AJOUT POUR LES LEVELS ET TAGS ---

        // Configure la relation entre Level et ses Requirements
        modelBuilder.Entity<LevelTagRequirement>()
            .HasOne(r => r.Level)
            .WithMany(l => l.LevelTagRequirements)
            .HasForeignKey(r => r.LevelId);
        // Filtre automatique : on ne récupère JAMAIS les éléments supprimés
        modelBuilder.Entity<Level>().HasQueryFilter(l => !l.IsDeleted);
       
        // Configure la relation entre Tag et les Requirements
        modelBuilder.Entity<LevelTagRequirement>()
            .HasOne(r => r.Tag)
            .WithMany(t => t.LevelTagRequirements)
            .HasForeignKey(r => r.TagId);
        modelBuilder.Entity<Tag>().HasQueryFilter(t => !t.IsDeleted);
        modelBuilder.Entity<LevelTagRequirement>().HasQueryFilter(lr => !lr.IsDeleted);

}
}