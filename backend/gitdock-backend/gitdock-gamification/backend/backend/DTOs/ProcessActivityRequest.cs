using System.ComponentModel.DataAnnotations;

namespace backend.DTOs
{
    public class ProcessActivityRequest
    {
        [Required(ErrorMessage = "L'ID de l'utilisateur est requis")]
        public long UserId { get; set; }

        [Required(ErrorMessage = "Le type d'activité (commit, pr, bugfix) est requis")]
        // Ce champ permet de calculer l'XP via la table XpConfigs
        public string ActivityType { get; set; } = string.Empty;
    }
}