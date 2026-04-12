using backend.Data;
using backend.Services;
using Microsoft.EntityFrameworkCore;
using Scalar.AspNetCore;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();
// 1. Enregistrer le DbContext avec PostgreSQL
builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// 2. Enregistrer ton Service (C'est crucial !)

builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        // Cela force l'API à envoyer "title" au lieu de "Title"
        options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    });
// 3. Activer le CORS (Pour que ton Vue.js puisse parler au Back-end)
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        builder =>
        {
            builder.AllowAnyOrigin()
                   .AllowAnyMethod()
                   .AllowAnyHeader();
        });
});
builder.Services.AddScoped<BadgeService>();
builder.Services.AddScoped<LevelService>();
builder.Services.AddScoped<TagService>();
builder.Services.AddScoped<XpConfigService>();

var app = builder.Build();
app.UseCors("AllowAll");
// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.MapScalarApiReference(options =>
    {
        options.WithTitle("GitDock Gamification API")
               .WithTheme(ScalarTheme.Purple) // Le thème Purple irait bien avec ton design
               .WithDefaultHttpClient(ScalarTarget.CSharp, ScalarClient.HttpClient);
    });
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
