-- Insure all SEQUENCES has the right number
SELECT setval('vdc_options_seq', max(option_id)) FROM vdc_options;
SELECT setval('custom_actions_seq', max(action_id)) FROM custom_actions;
SELECT setval('vdc_db_log_seq', max(error_id)) FROM vdc_db_log;
SELECT setval('audit_log_seq', max(audit_log_id)) FROM audit_log;
SELECT setval('schema_version_seq', max(id)) FROM schema_version;
