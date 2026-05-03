using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Data;

public class ApplicationDbContext : DbContext
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options) { }

    // --- Tables de configuration (Statiques) ---
    public DbSet<Badge> Badges { get; set; }
    public DbSet<Level> Levels { get; set; }
    public DbSet<Tag> Tags { get; set; }
    public DbSet<XpConfig> XpConfigs { get; set; }
    public DbSet<LevelTagRequirement> LevelTagRequirements { get; set; }

    // --- Tables Utilisateurs (Dynamiques) ---
    public DbSet<UserProgress> UserProgresses { get; set; }
    public DbSet<UserBadge> UserBadges { get; set; }
    public DbSet<UserTagProgress> UserTagProgresses { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // --- 1. CONFIGURATION DES ENUMS (Conversions string) ---
        modelBuilder.Entity<Badge>()
            .Property(b => b.Type)
            .HasConversion<string>();

        modelBuilder.Entity<Tag>()
            .Property(t => t.Type)
            .HasConversion<string>();

        // --- 2. FILTRES GLOBAUX (Soft Delete) ---
        // On ne récupère jamais les éléments supprimés logiquement
        modelBuilder.Entity<Badge>().Property(b => b.Type).HasConversion<string>();
        modelBuilder.Entity<Badge>().HasQueryFilter(b => b.DeletedAt == null);
        modelBuilder.Entity<Level>().HasQueryFilter(l => !l.IsDeleted);
        modelBuilder.Entity<Tag>().HasQueryFilter(t => !t.IsDeleted);
        modelBuilder.Entity<LevelTagRequirement>().HasQueryFilter(lr => !lr.IsDeleted);

        // --- 3. RELATIONS N-N (Configuration/Requirements) ---
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

        // --- 4. RELATIONS DE PROGRESSION UTILISATEUR (Indispensable pour le Leaderboard) ---

        // Clé primaire personnalisée pour UserProgress
        modelBuilder.Entity<UserProgress>()
            .HasKey(p => p.UserId);

        // Relation UserProgress <-> UserBadge
        modelBuilder.Entity<UserBadge>()
            .HasOne(ub => ub.UserProgress)
            .WithMany(up => up.UserBadges)
            .HasForeignKey(ub => ub.UserId);

        // Relation UserProgress <-> UserTagProgress
        modelBuilder.Entity<UserTagProgress>()
            .HasOne(utp => utp.UserProgress)
            .WithMany(up => up.UserTagProgresses)
            .HasForeignKey(utp => utp.UserId);

        // Dans OnModelCreating, ajoute ceci :
        modelBuilder.Entity<UserProgress>()
            .HasOne(up => up.Level)
            .WithMany() // Un niveau peut avoir plusieurs utilisateurs
            .HasForeignKey(up => up.CurrentLevelId);
    }
}