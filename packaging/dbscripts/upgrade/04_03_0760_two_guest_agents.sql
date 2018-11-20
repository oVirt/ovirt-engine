SELECT fn_db_rename_column('vm_dynamic', 'guest_agent_status', 'ovirt_guest_agent_status');
SELECT fn_db_add_column('vm_dynamic', 'qemu_guest_agent_status', 'INTEGER DEFAULT 0');
