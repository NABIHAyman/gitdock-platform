
```
gitdock-gamification
├─ backend
│  ├─ backend
│  │  ├─ appsettings.Development.json
│  │  ├─ appsettings.json
│  │  ├─ backend.csproj
│  │  ├─ backend.http
│  │  ├─ Controllers
│  │  │  ├─ BadgesController.cs
│  │  │  ├─ LevelsController.cs
│  │  │  ├─ TagsController.cs
│  │  │  ├─ WeatherForecastController.cs
│  │  │  └─ XpConfigController.cs
│  │  ├─ Data
│  │  │  └─ ApplicationDbContext.cs
│  │  ├─ Domain
│  │  │  ├─ Badge.cs
│  │  │  ├─ BadgeType.cs
│  │  │  ├─ Level.cs
│  │  │  ├─ LevelTagRequirement.cs
│  │  │  ├─ Tag.cs
│  │  │  └─ XpConfig.cs
│  │  ├─ DTOs
│  │  │  ├─ AddLevelRequirementDto.cs
│  │  │  ├─ BadgeResponseDto.cs
│  │  │  ├─ CreateBadgeDto.cs
│  │  │  ├─ CreateLevelDto.cs
│  │  │  ├─ CreateTagDto.cs
│  │  │  ├─ LevelResponseDto.cs
│  │  │  ├─ TagResponseDto.cs
│  │  │  └─ XpConfigDto.cs
│  │  ├─ Mappers
│  │  │  ├─ BadgeMapper.cs
│  │  │  ├─ LevelMapper.cs
│  │  │  ├─ TagMapper.cs
│  │  │  └─ XpConfigMapper.cs
│  │  ├─ Migrations
│  │  │  ├─ 20260329134601_InitialCreate.cs
│  │  │  ├─ 20260329134601_InitialCreate.Designer.cs
│  │  │  ├─ 20260329145129_RenameXpColumn.cs
│  │  │  ├─ 20260329145129_RenameXpColumn.Designer.cs
│  │  │  ├─ 20260405120830_AddTagsAndLevelsSchema.cs
│  │  │  ├─ 20260405120830_AddTagsAndLevelsSchema.Designer.cs
│  │  │  ├─ 20260405122216_FixRelationsTagsLevels.cs
│  │  │  ├─ 20260405122216_FixRelationsTagsLevels.Designer.cs
│  │  │  ├─ 20260405122439_AddLevelsAndTagsFullSchema.cs
│  │  │  ├─ 20260405122439_AddLevelsAndTagsFullSchema.Designer.cs
│  │  │  ├─ 20260405131944_AddSoftDeleteAndAuditFields.cs
│  │  │  ├─ 20260405131944_AddSoftDeleteAndAuditFields.Designer.cs
│  │  │  ├─ 20260405132143_UpdateSoftDeleteAndAuditFields.cs
│  │  │  ├─ 20260405132143_UpdateSoftDeleteAndAuditFields.Designer.cs
│  │  │  ├─ 20260406085324_AddXpConfig.cs
│  │  │  ├─ 20260406085324_AddXpConfig.Designer.cs
│  │  │  └─ ApplicationDbContextModelSnapshot.cs
│  │  ├─ Program.cs
│  │  ├─ Properties
│  │  │  └─ launchSettings.json
│  │  └─ Services
│  │     ├─ BadgeService.cs
│  │     ├─ LevelService.cs
│  │     ├─ TagService.cs
│  │     └─ XpConfigService.cs
│  └─ backend.sln
└─ frontend
   ├─ frontend.iml
   ├─ index.html
   ├─ package-lock.json
   ├─ package.json
   ├─ postcss.config.js
   ├─ public
   │  └─ favicon.ico
   ├─ README.md
   ├─ src
   │  ├─ App.vue
   │  ├─ assets
   │  │  └─ main.css
   │  ├─ components
   │  │  └─ dashboard
   │  │     ├─ manager
   │  │     │  ├─ BadgeSection.vue
   │  │     │  ├─ LevelSection.vue
   │  │     │  └─ TagSection.vue
   │  │     └─ user
   │  │        ├─ UserBadges.vue
   │  │        ├─ UserHeader.vue
   │  │        └─ UserStats.vue
   │  ├─ main.ts
   │  ├─ router
   │  │  └─ index.ts
   │  ├─ Services
   │  │  ├─ BadgeService.ts
   │  │  ├─ LevelService.ts
   │  │  └─ TagService.ts
   │  └─ views
   │     ├─ DashboardView.vue
   │     └─ UserDashboardView.vue
   ├─ tailwind.config.js
   ├─ tsconfig.json
   └─ vite.config.ts

```