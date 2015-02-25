-- Remove leftovers of vm_interface_statistics orphan childs.
DELETE FROM vm_interface_statistics
WHERE id NOT IN (SELECT id from vm_interface);

SELECT fn_db_create_constraint('vm_interface_statistics', 'fk_vm_interface_statistics_vm_interface', 'FOREIGN KEY (id) REFERENCES vm_interface(id) ON DELETE CASCADE');
