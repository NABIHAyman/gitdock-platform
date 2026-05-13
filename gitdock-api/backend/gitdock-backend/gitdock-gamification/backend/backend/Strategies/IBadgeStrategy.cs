using backend.Domain;
namespace backend.Strategies;

public interface IBadgeStrategy
{
    bool IsEligible(UserProgress progress, Badge badge);
}