-- CUSTOMER_BASE rolünden cüzdan yönetim ve okuma yetkilerini (can_manage_payment, can_read_payment) kaldırıyoruz.
-- Çünkü customer kendi cüzdanına zaten Self-Access bypass ile erişir, ekstra role ihtiyacı yoktur.
-- Eğer bu yetkiler global rolde (CUSTOMER_BASE) kalırsa, tüm restoranların cüzdanlarına müdahale edebilirler!

DELETE FROM "identity".role_permissions rp
USING "identity".roles r, "identity".permissions p
WHERE rp.role_id = r.id 
  AND rp.permission_id = p.id
  AND r.name = 'CUSTOMER_BASE'
  AND p.code IN ('can_manage_payment', 'can_read_payment');
