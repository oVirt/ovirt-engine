-- This script is commented out since any network can be the management
-- network from 4.0 and the logic should be :
-- If a display network is not set by any network, set it to the management network
-- If a migration network is not set by any network, set it to the management network

SELECT 04010280;
/*
CREATE FUNCTION __temp_set_display_migration() RETURNS VOID AS $$
DECLARE
    mgmt_name CHARACTER VARYING(15);
BEGIN
    SELECT option_value
    FROM vdc_options
    WHERE option_name='DefaultManagementNetwork'
    INTO mgmt_name;

    UPDATE network_cluster nc1
    SET is_display = true
    WHERE EXISTS (SELECT 1
                  FROM network
                  WHERE network.id = nc1.network_id AND name = mgmt_name)
        AND NOT EXISTS (SELECT 1
                        FROM network_cluster nc2
                        WHERE nc2.cluster_id = nc1.cluster_id AND nc2.is_display);

    UPDATE network_cluster nc1
    SET migration = true
    WHERE EXISTS (SELECT 1
                  FROM network
                  WHERE network.id = nc1.network_id AND name = mgmt_name)
        AND NOT EXISTS (SELECT 1
                        FROM network_cluster nc2
                        WHERE nc2.cluster_id = nc1.cluster_id AND nc2.migration);
END;
$$ LANGUAGE plpgsql;

SELECT __temp_set_display_migration();

DROP FUNCTION __temp_set_display_migration();
*/

