-- 1. 'can_create_order' ve 'can_read_order' izinleri ekleniyor
INSERT INTO "identity".permissions (id, code, description)
VALUES (gen_random_uuid(), 'can_create_order', 'Sipariş oluşturma yetkisi')
ON CONFLICT (code) DO NOTHING;

INSERT INTO "identity".permissions (id, code, description)
VALUES (gen_random_uuid(), 'can_read_order', 'Sipariş okuma yetkisi')
ON CONFLICT (code) DO NOTHING;

-- 'can_manage_payment' ve 'can_read_payment' yetkileri önceden V14'te eklendi, on conflict do nothing olduğu için eklenebilir veya ID'si alınabilir
INSERT INTO "identity".permissions (id, code, description)
VALUES (gen_random_uuid(), 'can_manage_payment', 'Cüzdan yönetimi yetkisi')
ON CONFLICT (code) DO NOTHING;

INSERT INTO "identity".permissions (id, code, description)
VALUES (gen_random_uuid(), 'can_read_payment', 'Cüzdan okuma yetkisi')
ON CONFLICT (code) DO NOTHING;

-- 2. CUSTOMER_BASE rolüne (name = 'CUSTOMER_BASE') bu yetkiler bağlanıyor
INSERT INTO "identity".role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM "identity".roles r, "identity".permissions p
WHERE r.name = 'CUSTOMER_BASE'
  AND p.code IN ('can_create_order', 'can_read_order', 'can_manage_payment', 'can_read_payment')
ON CONFLICT DO NOTHING;
