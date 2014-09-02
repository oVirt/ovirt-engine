-- add network qos columns to qos table
SELECT fn_db_add_column('vds_statistics', 'cpu_over_commit_time_stamp', 'TIMESTAMP WITH TIME ZONE');

update vds_statistics set cpu_over_commit_time_stamp = (select cpu_over_commit_time_stamp from vds_dynamic where vds_dynamic.vds_id = vds_statistics.vds_id);

SELECT fn_db_drop_column('vds_dynamic', 'cpu_over_commit_time_stamp');
