using System.ComponentModel.DataAnnotations;

namespace backend.DTOs
{
    public class TagActivityRequest
    {
        [Required(ErrorMessage = "L'ID de l'utilisateur est requis")]
        public long UserId { get; set; }

        [Required(ErrorMessage = "L'ID du Tag est requis")]
        // Ici le TagId est obligatoire car on cible une compétence précise
        public Guid TagId { get; set; }
    }
}