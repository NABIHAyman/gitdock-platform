using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace backend.Domain;

public class UserProgress
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.None)] // Car l'ID vient du service Auth
    public long UserId { get; set; }

    public int TotalExperience { get; set; } = 0;

    public Guid CurrentLevelId { get; set; }

    [ForeignKey("CurrentLevelId")]
    public virtual Level? Level { get; set; }

    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public virtual ICollection<UserBadge> UserBadges { get; set; } = new List<UserBadge>();
    public virtual ICollection<UserTagProgress> UserTagProgresses { get; set; } = new List<UserTagProgress>();
}