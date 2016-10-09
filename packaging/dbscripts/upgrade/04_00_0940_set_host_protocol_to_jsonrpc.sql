SELECT fn_db_delete_config_value('ProtocolFallbackRetries', 'general');
SELECT fn_db_delete_config_value('ProtocolFallbackTimeoutInMilliSeconds', 'general');

UPDATE vds_static
SET protocol = 1
WHERE protocol != 1
AND EXISTS (SELECT 1
            FROM cluster
            WHERE vds_static.cluster_id = cluster.cluster_id
            AND compatibility_version >= '3.6');
