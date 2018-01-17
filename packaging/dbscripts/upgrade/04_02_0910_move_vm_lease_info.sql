SELECT fn_db_add_column('vm_dynamic', 'lease_info', 'character varying(1000)');

UPDATE vm_dynamic
SET lease_info = vm_static.lease_info
FROM vm_static
WHERE vm_dynamic.vm_guid = vm_static.vm_guid;

SELECT fn_db_drop_column('vm_static', 'lease_info');

