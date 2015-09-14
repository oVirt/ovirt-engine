select fn_db_add_column('storage_pool', 'is_local', 'boolean');

create or replace function __temp_update_storage_domain_is_local() returns void
as $function$
begin
	if (exists (select 1 from information_schema.columns where table_schema = 'public' AND table_name ilike 'storage_pool' and column_name ilike 'storage_pool_type')) then
		update storage_pool set is_local = (storage_pool_type=4);
	end if;
end; $function$
language plpgsql;

select __temp_update_storage_domain_is_local();

drop function __temp_update_storage_domain_is_local();

ALTER TABLE storage_pool ALTER COLUMN storage_pool_type DROP NOT NULL;

