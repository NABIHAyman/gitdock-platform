using backend.Domain;
namespace backend.Strategies;

public class XpBadgeStrategy : IBadgeStrategy
{
    public bool IsEligible(UserProgress progress, Badge badge)
    {
        return progress.TotalExperience >= badge.Xp;
    }
}