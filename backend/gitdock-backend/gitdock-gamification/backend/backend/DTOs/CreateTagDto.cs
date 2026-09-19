namespace backend.DTOs
{
    // Utilisé pour le POST (Création)
    public class CreateTagDto
    {
        public string Name { get; set; } = string.Empty;
        public string Color { get; set; } = "#5b13ec";
        public string Type { get; set; } = "Commit";
    }
}
