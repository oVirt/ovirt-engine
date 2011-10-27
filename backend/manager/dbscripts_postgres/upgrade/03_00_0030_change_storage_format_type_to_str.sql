-- Change a storage_pool_format_type from integer to varchar
-- Change a storage_domain_format_type from integer to varchar
-- The changes were done due to changes in API between backend and vdsm

select fn_db_change_column_type('storage_pool','storage_pool_format_type','integer','varchar(50)');
select fn_db_change_column_type('storage_domain_static','storage_domain_format_type','integer','varchar(50)');
