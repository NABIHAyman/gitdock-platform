using backend.Enums;
namespace backend.Strategies;

public class BadgeStrategyFactory
{
    public IBadgeStrategy GetStrategy(BadgeType type)
    {
        return type switch
        {
            BadgeType.Auto => new XpBadgeStrategy(),
            BadgeType.Manual => new ManualBadgeStrategy(),
            _ => throw new ArgumentException("Type de badge non géré")
        };
    }
}