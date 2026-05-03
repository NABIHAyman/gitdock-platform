-- =============================================
-- 1. PROJETS (Liés aux entreprises 1 et 2 de gitdock-auth)
-- =============================================
INSERT INTO project (id, company_id, name, description, managed_by_id, url, platform, visibility, created_at, updated_at) VALUES
                                                                                                                              (1, 1, 'Finance ERP Core', 'Gestion comptable', 2, 'https://github.com/corpsolutions/finance-erp', 'GITHUB', 'PRIVATE', NOW(), NOW()),
                                                                                                                              (2, 1, 'HR Compliance', 'Outil RH', 2, 'https://github.com/corpsolutions/hr-compliance', 'GITHUB', 'PRIVATE', NOW(), NOW()),
                                                                                                                              (3, 2, 'Dungeon Crawler', 'RPG Hardcore', 5, 'https://github.com/pixelforge/dungeon-crawler', 'GITHUB', 'PUBLIC', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 2. PARTS (Divisions de projets)
-- =============================================
INSERT INTO part (id, project_id, name, description, created_at, updated_at) VALUES
                                                                                 (1, 1, 'Accounting Module', 'Financial accounting module', NOW(), NOW()),
                                                                                 (2, 1, 'Reporting Module', 'Financial reporting module', NOW(), NOW()),
                                                                                 (3, 3, 'Game Engine', 'Core game engine', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 3. BRANCHES
-- =============================================
INSERT INTO branch (id, project_id, name, created_at, updated_at) VALUES
                                                                      (1, 1, 'master', NOW(), NOW()),
                                                                      (2, 1, 'develop', NOW(), NOW()),
                                                                      (3, 3, 'main', NOW(), NOW()),
                                                                      (4, 3, 'feature/boss-fight', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 4. COMMITS (Histoires)
-- L'auteur est stocké en brut (Nom Git local)
-- =============================================
INSERT INTO commits (id, user_id, project_id, branch_id, hash, message, author_name, author_email, committed_at, created_at, updated_at) VALUES
                                                                                                                                             (1, 2, 1, 1, 'c_hash_1', 'feat: SOX module init', 'Jean-Pierre', 'jp@corp.com', NOW() - INTERVAL '10 DAYS', NOW(), NOW()),
                                                                                                                                             (2, 2, 1, 2, 'c_hash_2', 'fix: NullPointer payroll', 'Jean-Pierre', 'jp@corp.com', NOW() - INTERVAL '9 DAYS', NOW(), NOW()),
                                                                                                                                             (3, 5, 3, 3, 'p_hash_1', 'feat: Double jump', 'Neo', 'neo@pixel.gg', NOW() - INTERVAL '15 DAYS', NOW(), NOW()),
                                                                                                                                             (4, 5, 3, 4, 'p_hash_2', 'nerf: Boss HP reduced', 'Neo', 'neo@pixel.gg', NOW() - INTERVAL '14 DAYS', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 5. USER_PROJECT (Lien entre Utilisateurs Auth et Projets)
-- Le rôle 4 correspond au 'ROLE_MANAGER' dans l'auth
-- =============================================
INSERT INTO user_project (id, user_id, project_id, role_name, assigned_by_id, created_at) VALUES
                                                                                            (1, 2, 1, 'MANAGER', 2, NOW()),
                                                                                            (2, 2, 2, 'MANAGER', 2, NOW()),
                                                                                            (3, 5, 3, 'MANAGER', 5, NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================
-- 6. RECALIBRAGE DES SEQUENCES
-- =============================================
SELECT setval('project_id_seq', (SELECT MAX(id) FROM project));
SELECT setval('part_id_seq', (SELECT MAX(id) FROM part));
SELECT setval('branch_id_seq', (SELECT MAX(id) FROM branch));
SELECT setval('commits_id_seq', (SELECT MAX(id) FROM commits));
SELECT setval('user_project_id_seq', (SELECT MAX(id) FROM user_project));