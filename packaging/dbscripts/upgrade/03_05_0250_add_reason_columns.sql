select fn_db_add_column('vm_dynamic', 'reason', 'text');
select fn_db_add_column('vds_groups', 'optional_reason', 'boolean not null default false');
