-- Yeni yetki ekle
INSERT INTO permissions (id, code, description, domain)
VALUES (
    gen_random_uuid(),
    'can_add_personnel',
    'Allows user to add new personnel to a restaurant',
    'RESTAURANT'
);
