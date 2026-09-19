# Étape 1 : Build du projet
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

COPY ["backend.csproj", "./"]
RUN dotnet restore "backend.csproj"

COPY . .
RUN dotnet publish "backend.csproj" -c Release -o /app/publish

# Étape 2 : Runtime
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app

COPY --from=build /app/publish .

# Variables d'environnement pour forcer le port 5292
ENV ASPNETCORE_URLS=http://+:5292
ENV ASPNETCORE_HTTP_PORTS=5292

EXPOSE 5292

ENTRYPOINT ["dotnet", "backend.dll"]