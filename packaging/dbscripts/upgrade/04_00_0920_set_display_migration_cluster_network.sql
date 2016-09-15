--If a display network is not set by any network except the management one , set it to the management network
--If a migration network is not set by any network except the management one , set it to the management network

UPDATE network_cluster nc1
SET
  is_display = NOT EXISTS(SELECT 1
                          FROM network_cluster nc2
                          WHERE nc1.cluster_id = nc2.cluster_id AND is_display AND NOT management)
WHERE management AND NOT is_display;

UPDATE network_cluster nc1
SET
  migration = NOT EXISTS(SELECT 1
                         FROM network_cluster nc2
                         WHERE nc1.cluster_id = nc2.cluster_id AND migration AND NOT management)
WHERE management AND NOT migration;
