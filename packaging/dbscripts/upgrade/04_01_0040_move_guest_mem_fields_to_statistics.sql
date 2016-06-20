SELECT fn_db_add_column('vm_statistics', 'guest_mem_free', 'INTEGER DEFAULT NULL');
SELECT fn_db_add_column('vm_statistics', 'guest_mem_buffered', 'INTEGER DEFAULT NULL');
SELECT fn_db_add_column('vm_statistics', 'guest_mem_cached', 'INTEGER DEFAULT NULL');

UPDATE vm_statistics
SET guest_mem_free = (
                     SELECT guest_mem_free
                     FROM vm_dynamic
                     WHERE vm_statistics.vm_guid = vm_dynamic.vm_guid
                     );

UPDATE vm_statistics
SET guest_mem_buffered = (
                         SELECT guest_mem_buffered
                         FROM vm_dynamic
                         WHERE vm_statistics.vm_guid = vm_dynamic.vm_guid
                         );

UPDATE vm_statistics
SET guest_mem_cached = (
                       SELECT guest_mem_cached
                       FROM vm_dynamic
                       WHERE vm_statistics.vm_guid = vm_dynamic.vm_guid
                       );

SELECT fn_db_drop_column('vm_dynamic', 'guest_mem_free');
SELECT fn_db_drop_column('vm_dynamic', 'guest_mem_buffered');
SELECT fn_db_drop_column('vm_dynamic', 'guest_mem_cached');

