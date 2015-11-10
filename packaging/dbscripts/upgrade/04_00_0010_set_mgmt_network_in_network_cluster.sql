UPDATE network_cluster nc1
SET management = TRUE
WHERE network_id IN (SELECT id
                     FROM network
                     WHERE name = (SELECT COALESCE((SELECT option_value
                                                    FROM vdc_options
                                                    WHERE option_name = 'DefaultManagementNetwork'
                                                          AND version = 'general'),
                                                   'ovirtmgmt')))
      AND NOT exists(SELECT 1
                     FROM network_cluster nc2
                     WHERE nc1.cluster_id = nc2.cluster_id
                           AND nc2.management = TRUE);
