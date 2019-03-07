UPDATE vm_interface SET vm_guid = vmt_guid
    WHERE vm_guid IS NULL;

ALTER TABLE vm_interface ALTER COLUMN vm_guid SET NOT NULL;
SELECT fn_db_drop_column('vm_interface', 'vmt_guid');
