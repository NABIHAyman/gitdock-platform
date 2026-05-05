using backend.Data;
using backend.Services;
using Microsoft.EntityFrameworkCore;
using Scalar.AspNetCore;
using System.Text.Json;
using Steeltoe.Discovery.Client;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using backend.Repositories;
using backend.Strategies;
using backend.Clients;
using MassTransit;
using backend.Messaging.Consumers;
using backend.Messaging.Producers;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

// --- 1. CONFIGURATION SÉCURITÉ (JWT) ---
var jwtKey = "***REMOVED***";
var key = Encoding.UTF8.GetBytes(jwtKey);

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.RequireHttpsMetadata = false;
    options.SaveToken = true;
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),
        ValidateIssuer = false,
        ValidateAudience = false,
        ClockSkew = TimeSpan.Zero
    };
});

// --- 2. CONFIGURATION MASSTRANSIT & RABBITMQ ---
builder.Services.AddMassTransit(x =>
{
    // Ajout de tous tes consommateurs
    x.AddConsumer<UserCreatedConsumer>();
    x.AddConsumer<GamificationConsumer>();
    x.AddConsumer<PullRequestMergedConsumer>();
    x.AddConsumer<BugFixedConsumer>();
    x.AddConsumer<CommitSavedConsumer>();
    x.AddConsumer<CollaboratorAddedConsumer>();

    x.UsingRabbitMq((context, cfg) =>
    {
        cfg.Host(builder.Configuration["RabbitMQ:HostName"] ?? "localhost", "/", h =>
        {
            h.Username("guest");
            h.Password("guest");
        });

        // CORRECTIF CRITIQUE : Accepter les messages JSON bruts sans enveloppe MassTransit
        // C'est ce qui règle l'erreur "Value cannot be null. (Parameter 'envelope')"
        cfg.UseRawJsonSerializer();

        // Endpoint pour la création d'utilisateur
        cfg.ReceiveEndpoint("user-created-event-queue", e =>
        {
            e.ConfigureConsumer<UserCreatedConsumer>(context);
        });

        // ENDPOINT PRINCIPAL POUR LA GAMIFICATION
        cfg.ReceiveEndpoint("gamification-events-queue", e =>
        {
            // Liaison avec l'exchange de ton service Java
            e.Bind("gitdock.exchange", s =>
            {
                s.RoutingKey = "commit.saved.event";
                s.ExchangeType = "topic";
            });

            e.Bind("gitdock.exchange", s =>
            {
                s.RoutingKey = "pr.merged.event";
                s.ExchangeType = "topic";
            });

            // Configuration des consommateurs sur cette queue
            e.ConfigureConsumer<GamificationConsumer>(context);
            e.ConfigureConsumer<PullRequestMergedConsumer>(context);
            e.ConfigureConsumer<BugFixedConsumer>(context);
            e.ConfigureConsumer<CommitSavedConsumer>(context);
        });
        cfg.ReceiveEndpoint("collaborator-added-queue", e =>
        {
            e.Bind("gitdock.exchange", s =>
            {
                s.RoutingKey = "collaborator.added.event";
                s.ExchangeType = "topic";
            });
            e.ConfigureConsumer<CollaboratorAddedConsumer>(context);
        });
    });
});

// --- 3. SERVICES & REPOSITORIES ---
builder.Services.AddHttpContextAccessor();

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddHttpClient<IAuthServiceClient, AuthServiceClient>(client =>
{
    client.BaseAddress = new Uri("http://host.docker.internal:8081/");
    client.DefaultRequestHeaders.Add("Accept", "application/json");
});

builder.Services.AddDiscoveryClient(builder.Configuration);

// Repositories
builder.Services.AddScoped<IBadgeRepository, BadgeRepository>();
builder.Services.AddScoped<ILevelRepository, LevelRepository>();
builder.Services.AddScoped<ITagRepository, TagRepository>();
builder.Services.AddScoped<IXpConfigRepository, XpConfigRepository>();
builder.Services.AddScoped<IUserProgressRepository, UserProgressRepository>();
builder.Services.AddScoped<IUserBadgeRepository, UserBadgeRepository>();
builder.Services.AddScoped<IUserTagProgressRepository, UserTagProgressRepository>();
// Repositories
builder.Services.AddScoped<IContributorRepository, ContributorRepository>();


// Services
builder.Services.AddScoped<INotificationProducer, NotificationProducer>();
builder.Services.AddScoped<XpConfigService>();
builder.Services.AddScoped<IBadgeService, BadgeService>();
builder.Services.AddScoped<IUserProgressService, UserProgressService>();
builder.Services.AddScoped<IUserTagProgressService, UserTagProgressService>();
builder.Services.AddScoped<IUserBadgeService, UserBadgeService>();
builder.Services.AddScoped<ILevelService, LevelService>();
builder.Services.AddScoped<ITagService, TagService>();
builder.Services.AddScoped<IContributorService, ContributorService>();

// Clients HTTP
builder.Services.AddHttpClient<IProjectServiceClient, ProjectServiceClient>(client =>
{
    client.BaseAddress = new Uri("http://host.docker.internal:8083/"); // port direct gitdock-project
});

builder.Services.AddSingleton<BadgeStrategyFactory>();

// --- 4. CONFIGURATION API & CORS ---
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
        options.JsonSerializerOptions.Converters.Add(new JsonStringEnumConverter());
    });

builder.Services.AddCors(options => {
    options.AddPolicy("AllowVueApp", policy => {
        policy.WithOrigins("http://localhost:5173")
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials();
    });
});

builder.Services.AddOpenApi();
builder.Services.AddStackExchangeRedisCache(options =>
{
    options.Configuration = builder.Configuration.GetConnectionString("Redis") ?? "localhost:6379";
    options.InstanceName = "GitDock_";
});

// --- 5. PIPELINE HTTP ---
var app = builder.Build();

// Intercepteur d'erreurs pour le débuggage console
app.Use(async (context, next) => {
     try {
         await next();
     }
     catch (Exception ex) {
         Console.WriteLine("###############################################");
         Console.WriteLine($"!!! ERREUR DÉTECTÉE : {ex.Message}");
         if (ex.InnerException != null)
             Console.WriteLine($"!!! CAUSE INTERNE : {ex.InnerException.Message}");
         Console.WriteLine("###############################################");
         throw;
     }
 });

app.UseCors("AllowVueApp");

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.MapScalarApiReference(options =>
    {
        options.WithDefaultHttpClient(ScalarTarget.CSharp, ScalarClient.HttpClient);
        options.Servers = new[] { new ScalarServer("http://localhost:5292") };
    });
}

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

// Migration automatique au démarrage
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
    context.Database.Migrate();
}

app.Run();