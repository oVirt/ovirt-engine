CREATE TABLE labels (
    label_id UUID NOT NULL,
    label_name VARCHAR(50) NOT NULL,
    read_only BOOLEAN DEFAULT FALSE NOT NULL,

    CONSTRAINT pk_labels_id PRIMARY KEY (label_id),
    CONSTRAINT label_name UNIQUE (label_name)
);

CREATE TABLE labels_map (
    label_id UUID NOT NULL REFERENCES labels (label_id) ON DELETE CASCADE ON UPDATE CASCADE,
    vm_id UUID REFERENCES vm_static (vm_guid) ON DELETE CASCADE ON UPDATE CASCADE,
    vds_id UUID REFERENCES vds_static (vds_id) ON DELETE CASCADE ON UPDATE CASCADE
);

SELECT fn_db_create_index('idx_labels_map_label_id', 'labels_map', 'label_id', '');
SELECT fn_db_create_index('idx_labels_map_vm_id', 'labels_map', 'vm_id', '');
SELECT fn_db_create_index('idx_labels_map_vds_id', 'labels_map', 'vds_id', '');
