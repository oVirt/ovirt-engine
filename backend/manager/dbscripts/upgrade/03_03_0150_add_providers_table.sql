
CREATE TABLE providers
(
    id UUID CONSTRAINT providers_pk PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(4000),
    url VARCHAR(512) NOT NULL,
    provider_type VARCHAR(32) NOT NULL,
    auth_required BOOLEAN NOT NULL,
    auth_username VARCHAR(64),
    auth_password TEXT,
    _create_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    _update_date TIMESTAMP WITH TIME ZONE
);

