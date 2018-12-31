-- Create vm_backups table
CREATE TABLE vm_backups (
    backup_id UUID NOT NULL PRIMARY KEY,
    from_checkpoint_id UUID,
    to_checkpoint_id UUID,
    vm_id UUID NOT NULL,
    phase VARCHAR(32) NOT NULL,
    _create_date TIMESTAMP WITH TIME ZONE DEFAULT now(),
    FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE,
    FOREIGN KEY (backup_id) REFERENCES command_entities(command_id) ON DELETE CASCADE
);

-- Create backup_disk_map table
CREATE TABLE vm_backup_disk_map (
    backup_id UUID NOT NULL,
    disk_id UUID NOT NULL,
    backup_url VARCHAR,
    PRIMARY KEY (backup_id, disk_id)
);

-- Create vm_checkpoints table
CREATE TABLE vm_checkpoints (
    checkpoint_id UUID NOT NULL PRIMARY KEY,
    parent_id UUID,
    vm_id UUID NOT NULL,
    _create_date TIMESTAMP WITH TIME ZONE DEFAULT now(),
    FOREIGN KEY (vm_id) REFERENCES vm_static(vm_guid) ON DELETE CASCADE
);

-- Create checkpoint_disk_map table
CREATE TABLE vm_checkpoint_disk_map (
    checkpoint_id UUID NOT NULL,
    disk_id UUID NOT NULL,
    PRIMARY KEY (checkpoint_id, disk_id)
);

-- Create indexes
SELECT fn_db_create_index('idx_vm_backups_vm_id', 'vm_backups', 'vm_id', '', false);
SELECT fn_db_create_index('idx_vm_checkpoints_vm_id', 'vm_checkpoints', 'vm_id', '', false);