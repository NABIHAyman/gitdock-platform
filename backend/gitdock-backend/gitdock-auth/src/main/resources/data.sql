-- 1. ENTREPRISES (Indispensable avant les utilisateurs)
INSERT INTO company (id, name, subscription_plan, is_personal, created_at, updated_at, is_deleted) VALUES
(999, 'GitDockCorp', 'FREE', false, NOW(), NOW(), false),
(1, 'CorpSolutions', 'FREE', false, NOW(), NOW(), false),
(2, 'PixelForge', 'PRO', false, NOW(), NOW(), false),
(3, 'GaiaCollective', 'ENTERPRISE', false, NOW(), NOW(), false)
    ON CONFLICT (id) DO NOTHING;

-- 2. RÔLES
INSERT INTO role (id, name, description)
VALUES
(1, 'ROLE_SUPER_ADMIN', 'Administrateur global'),
(2, 'ROLE_COMPANY_ADMIN', 'Administrateur entreprise'),
(3, 'ROLE_WORKSPACE_OWNER', 'Propriétaire personnel'),
(4, 'ROLE_MANAGER', 'Gestionnaire'),
(5, 'ROLE_DEVELOPER', 'Développeur'),
(6, 'ROLE_TESTER', 'Testeur'),
(7, 'ROLE_CONSULTANT', 'Consultant')
    ON CONFLICT (id) DO NOTHING;

-- 3. UTILISATEURS (Password = Admin123!)
INSERT INTO user_account (id, first_name, last_name, email, password, username, is_enabled, is_deleted, created_at, updated_at, account_locked, company_id)
VALUES
(1, 'Super', 'Admin', 'admin@gitdock.com', '$2a$10$KgRmdOkUbxrJyOzV8OibLuejvvmNjO/z9Tv3Hj3/pZM599a1iW2TG', 'admin', true, false, NOW(), NOW(), false, 999),
(2, 'Jean-Pierre', 'Directeur', 'jp@corp.com', '$2a$10$KgRmdOkUbxrJyOzV8OibLuejvvmNjO/z9Tv3Hj3/pZM599a1iW2TG', 'jp', true, false, NOW(), NOW(), false, 1),
(5, 'Neo', 'Anderson', 'neo@pixel.gg', '$2a$10$KgRmdOkUbxrJyOzV8OibLuejvvmNjO/z9Tv3Hj3/pZM599a1iW2TG', 'neo', true, false, NOW(), NOW(), false, 2)
    ON CONFLICT (id) DO NOTHING;

-- 4. ATTRIBUTION DES RÔLES
INSERT INTO user_role (id, user_id, role_id, company_id, assigned_at) VALUES
(1, 1, 1, NULL, NOW()),
(2, 2, 2, 1, NOW()),
(5, 5, 2, 2, NOW())
    ON CONFLICT (id) DO NOTHING;

-- 5. RECALIBRAGE DES SEQUENCES (Crucial pour que Hibernate ne plante pas aux prochains inserts)
SELECT setval('role_id_seq', (SELECT MAX(id) FROM role));
SELECT setval('company_id_seq', (SELECT MAX(id) FROM company));
SELECT setval('user_account_id_seq', (SELECT MAX(id) FROM user_account));
SELECT setval('user_role_id_seq', (SELECT MAX(id) FROM user_role));