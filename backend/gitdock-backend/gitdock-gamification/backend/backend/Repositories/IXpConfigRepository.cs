using backend.Domain;

namespace backend.Repositories;

public interface IXpConfigRepository
{
    Task<XpConfig?> GetCurrentConfigAsync();
    Task UpdateAsync(XpConfig config);
    Task SaveChangesAsync();
}