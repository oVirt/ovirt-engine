select fn_db_add_column('storage_domain_static', 'warning_low_space_indicator', 'INTEGER DEFAULT NULL');
select fn_db_add_column('storage_domain_static', 'critical_space_action_blocker', 'INTEGER DEFAULT NULL');

UPDATE storage_domain_static
  SET warning_low_space_indicator=cast((
       SELECT option_value
       FROM vdc_options
       WHERE option_name='WarningLowSpaceIndicator' AND version='general') AS INTEGER),
       critical_space_action_blocker=cast((
       SELECT option_value
       FROM vdc_options
       WHERE option_name='CriticalSpaceActionBlocker' AND version='general') AS INTEGER);
