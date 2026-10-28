-- V31__add_review_permissions.sql

-- 1. Insert review permissions
INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('b8c2e71d-5561-4682-a9b0-9c124316d111', 'can_reply_restaurant_review', 'Restorana yapılan müşteri değerlendirmesine tek seferlik resmi yanıt verme', 'ORDER', true, false),
('e9d3f82e-6672-4793-baa1-0d235427e222', 'can_delete_restaurant_review', 'Uygunsuz veya zararlı müşteri değerlendirmesini yayından kaldırma (soft delete)', 'ORDER', true, false)
ON CONFLICT (id) DO NOTHING;

-- 2. Bind review reply permission to RESTAURANT_OWNER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'RESTAURANT_OWNER'
  AND p.code = 'can_reply_restaurant_review'
ON CONFLICT DO NOTHING;

-- 3. Bind review deletion (moderation) permission to SYSTEM_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.code = 'can_delete_restaurant_review'
ON CONFLICT DO NOTHING;
