CREATE TABLE IF NOT EXISTS "payment".roles_replica (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    user_type VARCHAR(255),
    context_id UUID
);

CREATE TABLE IF NOT EXISTS "payment".permissions_replica (
    id UUID PRIMARY KEY,
    code VARCHAR(255),
    domain VARCHAR(255),
    is_active BOOLEAN,
    is_restricted BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS "payment".role_permissions_replica (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

ALTER TABLE "payment".permissions_replica ADD COLUMN IF NOT EXISTS is_active BOOLEAN;
ALTER TABLE "payment".permissions_replica ADD COLUMN IF NOT EXISTS is_restricted BOOLEAN;
ALTER TABLE "payment".permissions_replica ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE "payment".permissions_replica ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;
