CREATE TABLE mac_pools (
    id                            UUID    NOT NULL PRIMARY KEY,
    name                          CHARACTER VARYING(255),
    description                   CHARACTER VARYING(4000),
    allow_duplicate_mac_addresses BOOLEAN NOT NULL DEFAULT FALSE,
    default_pool                  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE mac_pool_ranges (
    mac_pool_id UUID    NOT NULL REFERENCES mac_pools (id) ON DELETE CASCADE,
    from_mac    CHARACTER VARYING(17) NOT NULL,
    to_mac      CHARACTER VARYING(17) NOT NULL
) WITH OIDS;

SELECT fn_db_add_column('storage_pool',
                        'mac_pool_id',
                        'UUID REFERENCES mac_pools (id)');

-- Insert the default pool, preserving the "AllowDuplicateMacAddresses" from config.
INSERT INTO mac_pools (id,
                      name,
                      allow_duplicate_mac_addresses,
                      description,
                      default_pool)
SELECT uuid_generate_v1(),
       'Default',
       (SELECT option_value :: BOOLEAN
        FROM   vdc_options
        WHERE  option_name = 'AllowDuplicateMacAddresses'),
       'Default MAC pool',
       TRUE;

-- Create a range record for the default pool for each range defined in vdc_options.
INSERT INTO mac_pool_ranges (mac_pool_id,
                       from_mac,
                       to_mac)
SELECT (SELECT id FROM mac_pools),
       mac_range[1],
       mac_range[2]
FROM (SELECT   string_to_array(unnest(string_to_array(option_value, ',')),'-') as mac_range
        FROM   vdc_options
        WHERE  option_name = 'MacPoolRanges') as mac_ranges;

-- Update all DCs to use the default pool.
UPDATE storage_pool
SET mac_pool_id = (SELECT id FROM mac_pools);

-- DC must be connected to a MAC pool.
ALTER TABLE storage_pool ALTER COLUMN mac_pool_id SET NOT NULL;

