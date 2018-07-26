SELECT fn_db_add_column('cluster', 'log_max_memory_used_threshold', 'INT NOT NULL DEFAULT 95');
SELECT fn_db_add_column('cluster', 'log_max_memory_used_threshold_type', 'SMALLINT NOT NULL DEFAULT 0');

--
-- We need to set log_max_memory_used_threshold for all clusters to the value in vdc_options
--

UPDATE cluster
  SET log_max_memory_used_threshold = (SELECT option_value::integer
                                       FROM vdc_options
                                       WHERE option_name = 'LogMaxPhysicalMemoryUsedThresholdInPercentage');
