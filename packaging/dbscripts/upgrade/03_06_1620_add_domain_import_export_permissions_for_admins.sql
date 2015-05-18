Create or replace FUNCTION __temp_insert_add_domain_import_export_permissions_for_admins()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_VM_IMPORTER_EXPORTER_ID UUID;

BEGIN
       v_VM_IMPORTER_EXPORTER_ID := 'def00030-0000-0000-0000-def000000011';

INSERT INTO roles (id, name, description, is_readonly, role_type, allows_viewing_children, app_mode) VALUES (v_VM_IMPORTER_EXPORTER_ID, 'VmImporterExporter', 'Administrator Role, with permission to import or export Vms', true, 1, true, 1);

PERFORM fn_db_add_action_group_to_role(v_VM_IMPORTER_EXPORTER_ID,8);

 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT  __temp_insert_add_domain_import_export_permissions_for_admins();
DROP function  __temp_insert_add_domain_import_export_permissions_for_admins();

