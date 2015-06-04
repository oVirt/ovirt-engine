--------------------------------------------------------------------------------------------
-- Refacotr vm_static.dedicated_vm_for_vds to contain multiple host uuids in CSV list
--------------------------------------------------------------------------------------------

-- Drop FK from host to vm_static --------------------------------------------------------------------------------------------
SELECT fn_db_drop_constraint('vm_static', 'fk_vds_static_vm_static');

-- Alter vm_static.dedicated_vm_for_vds to text field. Containing csv full of uuid
--------------------------------------------------------------------------------------------
SELECT fn_db_change_column_type('vm_static','dedicated_vm_for_vds','uuid','text');

