-- Dropping and creating vds_groups view in order to contain the change
DROP VIEW IF EXISTS vds_groups;
SELECT fn_db_drop_column ('cluster', 'optional_reason');
SELECT fn_db_drop_column ('cluster', 'maintenance_reason_required');
CREATE VIEW vds_groups AS SELECT * FROM cluster;
