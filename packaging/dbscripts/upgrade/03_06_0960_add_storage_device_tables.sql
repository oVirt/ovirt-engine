-- Add storage_device table
CREATE TABLE storage_device
(
    id UUID NOT NULL,
    name VARCHAR(1000) NOT NULL,
    device_uuid VARCHAR(38),
    filesystem_uuid VARCHAR(38),
    vds_id UUID NOT NULL,
    description VARCHAR(2000),
    device_type VARCHAR(50),
    device_path VARCHAR(4096),
    filesystem_type VARCHAR(50),
    mount_point VARCHAR(4096),
    size BIGINT,
    is_free BOOLEAN,
    _create_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    _update_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT LOCALTIMESTAMP,
    CONSTRAINT pk_storage_device PRIMARY KEY(id),
    FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE
) WITH OIDS;

select fn_db_create_index('IDX_storage_device_vds_id', 'storage_device', 'vds_id', '');
