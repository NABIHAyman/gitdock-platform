using System.Security.Claims;
using System.Text.Encodings.Web;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;

namespace backend.Security
{
    public class GatewayAuthenticationOptions : AuthenticationSchemeOptions { }

    public class GatewayAuthenticationHandler : AuthenticationHandler<GatewayAuthenticationOptions>
    {
        public GatewayAuthenticationHandler(
            IOptionsMonitor<GatewayAuthenticationOptions> options,
            ILoggerFactory logger,
            UrlEncoder encoder,
            ISystemClock clock)
            : base(options, logger, encoder, clock) { }

        protected override Task<AuthenticateResult> HandleAuthenticateAsync()
        {
            // 1. Lire le header injecté par la Gateway
            if (!Request.Headers.TryGetValue("X-User-Id", out var userIdValues))
            {
                return Task.FromResult(AuthenticateResult.NoResult());
            }

            var userId = userIdValues.FirstOrDefault();
            var roles = Request.Headers["X-User-Roles"].FirstOrDefault();

            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, userId ?? "")
            };

            // 2. Assigner les rôles
            if (!string.IsNullOrEmpty(roles) && roles != "null")
            {
                var cleanRoles = roles.Replace("[", "").Replace("]", "").Replace(" ", "");
                foreach (var role in cleanRoles.Split(','))
                {
                    if (!string.IsNullOrEmpty(role))
                        claims.Add(new Claim(ClaimTypes.Role, role));
                }
            }

            // 3. Valider l'identité pour .NET
            var identity = new ClaimsIdentity(claims, "Gateway");
            var principal = new ClaimsPrincipal(identity);
            var ticket = new AuthenticationTicket(principal, "Gateway");

            return Task.FromResult(AuthenticateResult.Success(ticket));
        }
    }
}