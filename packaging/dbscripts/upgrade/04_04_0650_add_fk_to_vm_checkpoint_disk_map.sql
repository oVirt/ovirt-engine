SELECT fn_db_create_constraint('vm_checkpoint_disk_map',
                               'fk_vm_checkpoint_disk_map_vm_checkpoints',
                               'FOREIGN KEY (checkpoint_id) REFERENCES vm_checkpoints(checkpoint_id) ON DELETE CASCADE');
