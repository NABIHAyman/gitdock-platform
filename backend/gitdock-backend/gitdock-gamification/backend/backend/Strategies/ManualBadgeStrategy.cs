using backend.Domain;
namespace backend.Strategies;

public class ManualBadgeStrategy : IBadgeStrategy
{
    public bool IsEligible(UserProgress progress, Badge badge) => false;
}