using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace backend.Domain
{
    public class UserBadge
    {
        [Key]
        public Guid Id { get; set; }

        [Required]
        public long UserId { get; set; } // Supprimé le = string.Empty;

        public Guid BadgeId { get; set; }

        [ForeignKey("BadgeId")]
        public virtual Badge? Badge { get; set; } // virtual est mieux pour le lazy loading

        public DateTime UnlockedAt { get; set; } = DateTime.UtcNow;

        [ForeignKey("UserId")]
        public virtual UserProgress UserProgress { get; set; } = default!;
    }
}