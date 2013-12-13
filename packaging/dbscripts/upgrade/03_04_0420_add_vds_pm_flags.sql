select fn_db_add_column('vds_static', 'disable_auto_pm', 'BOOLEAN default false');
select fn_db_add_column('vds_dynamic', 'controlled_by_pm_policy', 'BOOLEAN default false');
-- Enable automatic PM on all hosts in Up state
UPDATE vds_dynamic set controlled_by_pm_policy=true WHERE status=3;
