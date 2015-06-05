INSERT INTO gluster_config_master(config_key, config_description, minimum_supported_cluster, config_possible_values, config_feature)
 values('use_meta_volume', 'Meta volume for the geo-replication session', '3.5', 'true;false', 'geo_replication');

UPDATE gluster_config_master SET
  config_key=
  (CASE config_key
      WHEN 'log-file' THEN 'log_file'
      WHEN 'gluster-log-file' THEN 'gluster_log_file'
      WHEN 'ignore-deletes' THEN 'ignore_deletes'
      WHEN 'ssh-command' THEN 'ssh_command'
      ELSE config_key
  END);

UPDATE gluster_config_master SET config_possible_values='true;false' WHERE config_key='ignore_deletes';
