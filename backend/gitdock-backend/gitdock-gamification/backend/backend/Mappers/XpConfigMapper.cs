using backend.Domain;
using backend.DTOs;

namespace backend.Mappers
{
        public static class XpConfigMapper
        {
            public static XpConfigDto ToDto(this XpConfig entity)
            {
                return new XpConfigDto
                {
                    CommitXp = entity.CommitXp,
                    PrXp = entity.PrXp,
                    BugFixXp = entity.BugFixXp
                };
            }

            public static XpConfig ToEntity(this XpConfigDto dto)
            {
                return new XpConfig
                {
                    CommitXp = dto.CommitXp,
                    PrXp = dto.PrXp,
                    BugFixXp = dto.BugFixXp,
                    UpdatedAt = DateTime.UtcNow
                };
            }
        }
    }

