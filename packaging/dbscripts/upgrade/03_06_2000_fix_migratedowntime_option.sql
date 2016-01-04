-- Remove old wrong option name.
DELETE
FROM vdc_options
WHERE option_name = 'MigrateDowntime';
