using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace backend.Domain;

public class UserTagProgress
{
    [Key]
    public Guid Id { get; set; } = Guid.NewGuid();

    [Required]
    public long UserId { get; set; } // Changé de string à long

    public Guid TagId { get; set; }

    [ForeignKey("TagId")]
    public virtual Tag Tag { get; set; } = default!;

    public int Occurrences { get; set; } = 0;
    public DateTime LastUpdatedAt { get; set; } = DateTime.UtcNow;

    [ForeignKey("UserId")]
    public virtual UserProgress UserProgress { get; set; } = default!;
}