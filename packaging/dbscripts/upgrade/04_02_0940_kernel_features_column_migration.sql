-- vds_options.kernel_features column was simultaneously introduced in both 4.1 branch (as type VARCHAR) and 4.2 branch
-- (as type JSONB). This script unifies column type to JSONB for users migrating from 4.1.

SELECT fn_db_update_column_to_jsonb_compatible_values('vds_dynamic', 'kernel_features', NULL);
SELECT fn_db_change_column_type('vds_dynamic', 'kernel_features', 'character varying', 'jsonb USING kernel_features::jsonb');
