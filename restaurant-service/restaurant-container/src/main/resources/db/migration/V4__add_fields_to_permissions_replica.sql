CREATE TABLE IF NOT EXISTS restaurant.roles_replica (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    user_type VARCHAR(255),
    context_id UUID
);

CREATE TABLE IF NOT EXISTS restaurant.permissions_replica (
    id UUID PRIMARY KEY,
    code VARCHAR(255),
    domain VARCHAR(255),
    is_active BOOLEAN,
    is_restricted BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS restaurant.role_permissions_replica (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

ALTER TABLE restaurant.permissions_replica ADD COLUMN IF NOT EXISTS is_active BOOLEAN;
ALTER TABLE restaurant.permissions_replica ADD COLUMN IF NOT EXISTS is_restricted BOOLEAN;
ALTER TABLE restaurant.permissions_replica ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE restaurant.permissions_replica ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;
