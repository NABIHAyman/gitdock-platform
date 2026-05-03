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

var builder = WebApplication.CreateBuilder(args);

// --- 1. CONFIGURATION SÉCURITÉ (JWT) ---
var jwtKey = "***REMOVED***";
var key = Encoding.ASCII.GetBytes(jwtKey);

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
    x.AddConsumer<UserCreatedConsumer>();
    x.AddConsumer<GamificationConsumer>();
    x.AddConsumer<PullRequestMergedConsumer>();
    x.AddConsumer<BugFixedConsumer>();
    x.AddConsumer<CommitSavedConsumer>();

    x.UsingRabbitMq((context, cfg) =>
    {
        cfg.Host(builder.Configuration["RabbitMQ:HostName"] ?? "localhost", "/", h =>
        {
            h.Username("guest");
            h.Password("guest");
        });

        cfg.ReceiveEndpoint("user-created-event-queue", e =>
        {
            e.ConfigureConsumer<UserCreatedConsumer>(context);
        });

        cfg.ReceiveEndpoint("gamification-events-queue", e =>
        {
            e.ConfigureConsumer<GamificationConsumer>(context);
            e.ConfigureConsumer<PullRequestMergedConsumer>(context);
            e.ConfigureConsumer<BugFixedConsumer>(context);
            e.ConfigureConsumer<CommitSavedConsumer>(context);
        });
    });
});

// --- 3. SERVICES & REPOSITORIES ---
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

// Services
builder.Services.AddScoped<INotificationProducer, NotificationProducer>();
builder.Services.AddScoped<XpConfigService>();
builder.Services.AddScoped<IBadgeService, BadgeService>();
builder.Services.AddScoped<IUserProgressService, UserProgressService>();
builder.Services.AddScoped<IUserTagProgressService, UserTagProgressService>();
builder.Services.AddScoped<IUserBadgeService, UserBadgeService>();
builder.Services.AddScoped<ILevelService, LevelService>();
builder.Services.AddScoped<ITagService, TagService>();

builder.Services.AddSingleton<BadgeStrategyFactory>();

// --- 4. CONFIGURATION API & CORS ---
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    });

builder.Services.AddCors(options => {
    options.AddPolicy("AllowVueApp", policy => {
        policy.WithOrigins("http://localhost:5173") // Ton front-end
              .AllowAnyHeader()
              .AllowAnyMethod()
              .AllowCredentials(); // Important pour le passage des tokens/session
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

// Intercepteur d'erreurs pour le débuggage
app.Use(async (context, next) => {
     try {
         await next();
     }
     catch (Exception ex) {
         Console.WriteLine("###############################################");
         Console.WriteLine($"!!! ERREUR DÉTECTÉE : {ex.Message}");
         if (ex.InnerException != null)
             Console.WriteLine($"!!! CAUSE INTERNE : {ex.InnerException.Message}");
         Console.WriteLine($"!!! STACKTRACE : {ex.StackTrace}");
         Console.WriteLine("###############################################");
         throw;
     }
 });

app.UseCors("AllowVueApp"); // Utilise le même nom que la police définie plus haut

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

// Migration automatique
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
    context.Database.Migrate();
}

app.Run();