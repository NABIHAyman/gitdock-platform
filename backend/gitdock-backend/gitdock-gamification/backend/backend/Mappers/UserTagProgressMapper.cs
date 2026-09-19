using backend.Domain;
using backend.DTOs;

namespace backend.Mappers
{
    public static class UserTagProgressMapper
    {
        public static UserTagProgressDto ToDto(this UserTagProgress? entity)
        {
            if (entity == null) return null;

            return new UserTagProgressDto
            {
                TagName = entity.Tag?.Name ?? "Inconnu",
                Occurrences = entity.Occurrences
            };
        }
    }
}