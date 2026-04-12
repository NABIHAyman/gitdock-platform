namespace backend.DTOs
{
    // Utilisé pour le GET (Retourner la liste au Front)
    public class TagResponseDto
    {
        public Guid Id { get; set; }
        public string Name { get; set; } = string.Empty;
        public string Color { get; set; } = string.Empty;
        public string Type { get; set; } = string.Empty;
    }
}
