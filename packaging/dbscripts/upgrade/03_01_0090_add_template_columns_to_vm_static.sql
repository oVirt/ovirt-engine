-- Add column to distinguish what entity this is, currently either VM or TEMPLATE.
SELECT fn_db_add_column('vm_static', 'entity_type', 'VARCHAR(32)');

-- Add specific templates columns to vm_static.
SELECT fn_db_add_column('vm_static', 'child_count', 'INTEGER DEFAULT 0');
SELECT fn_db_add_column('vm_static', 'template_status', 'INTEGER');

-- Add the internal_drive_mapping field to image_templates since we don't want it in image_vm_map.
SELECT fn_db_add_column('image_templates', 'internal_drive_mapping', 'VARCHAR(50)');

