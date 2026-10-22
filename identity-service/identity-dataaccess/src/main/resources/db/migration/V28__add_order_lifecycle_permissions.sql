-- V28__add_order_lifecycle_permissions.sql

-- 1. Insert 12 new order lifecycle permissions with deterministic UUIDs
INSERT INTO permissions (id, code, description, domain, active, is_restricted) VALUES
('d5740d0f-889f-4686-b09c-711ea0cb8d9b', 'can_approve_order', 'Onay bekleyen siparişi kabul edip mutfak akışına alma', 'ORDER', true, false),
('ac31a2c0-3f6d-43cc-965b-3e21b06c9982', 'can_cancel_waiting_order', 'Onay bekleyen siparişi mutfağa almadan reddetme/iptal etme', 'ORDER', true, false),
('f672535c-1e8b-4d35-9e80-65f5fd4d4062', 'can_start_preparing_order', 'Onaylanmış siparişi tezgaha alıp hazırlığını fiilen başlatma', 'ORDER', true, false),
('3d410cbd-4f5c-4ad8-a01f-c25ab9c11b22', 'can_cancel_approved_order', 'Onaylanmış ancak henüz hazırlığı başlamamış siparişi iptal etme', 'ORDER', true, false),
('d29e901d-cb64-4e4b-b9ab-44c132da9992', 'can_ready_order', 'Hazırlığı biten siparişi kurye teslim alımına hazır hale getirme', 'ORDER', true, false),
('fa5b6200-9974-4d9e-91c7-92b8a12a64cc', 'can_cancel_preparing_order', 'Hazırlık aşamasındaki siparişi iptal etme', 'ORDER', true, false),
('c1e21ce6-892f-4e84-8237-a02680d4f619', 'can_start_delivery', 'Hazır siparişi kuryenin teslim alıp yola çıkması', 'ORDER', true, false),
('9664d3bb-5304-41d5-aff6-5c2f35a85c49', 'can_cancel_ready_order', 'Hazır bekleyen paketi kurye almadan önce restoranda iptal etme', 'ORDER', true, false),
('f2982f5d-f419-42c9-850b-22c2d6baff28', 'can_deliver_order', 'Siparişi kapıda müşteriye teslim edip süreci başarıyla kapatma', 'ORDER', true, false),
('6b97288c-c08c-490f-aabd-0d62fafee3e3', 'can_cancel_in_delivery_order', 'Yoldaki siparişi lojistik kaza veya adreste bulunamama ile iptal etme', 'ORDER', true, false),
('f973d844-a33d-43c2-82de-4f24c4ae9323', 'can_force_cancel_order', 'Sistem yöneticisi ve destek ekiplerinin zorunlu iptal edebilmesi', 'ORDER', true, false),
('4557c6e7-bae2-43d7-93fd-6b30a3b34025', 'can_read_all_orders', 'Sistem yöneticisi ve çağrı merkezi ekiplerinin herhangi bir siparişi sorgulayabilmesi', 'ORDER', true, false)
ON CONFLICT (id) DO NOTHING;

-- 2. Bind operational lifecycle permissions to RESTAURANT_OWNER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'RESTAURANT_OWNER'
  AND p.code IN (
    'can_approve_order',
    'can_cancel_waiting_order',
    'can_start_preparing_order',
    'can_cancel_approved_order',
    'can_ready_order',
    'can_cancel_preparing_order',
    'can_start_delivery',
    'can_cancel_ready_order',
    'can_deliver_order',
    'can_cancel_in_delivery_order'
  )
ON CONFLICT DO NOTHING;

-- 3. Bind admin lifecycle permissions to SYSTEM_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.code IN ('can_force_cancel_order', 'can_read_all_orders')
ON CONFLICT DO NOTHING;

-- 4. Remove can_read_orders from SYSTEM_ADMIN role
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE name = 'SYSTEM_ADMIN')
  AND permission_id = 'e319b310-ffa9-427a-b520-a05944166f1d';

-- 5. Delete can_review_orders from role_permissions and permissions
DELETE FROM role_permissions
WHERE permission_id IN (SELECT id FROM permissions WHERE code = 'can_review_orders');

DELETE FROM permissions
WHERE code = 'can_review_orders';
