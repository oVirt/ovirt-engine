select fn_db_add_column('disk_image_dynamic', 'read_latency_seconds', 'DECIMAL(18,9)');
select fn_db_add_column('disk_image_dynamic', 'write_latency_seconds', 'DECIMAL(18,9)');
select fn_db_add_column('disk_image_dynamic', 'flush_latency_seconds', 'DECIMAL(18,9)');


