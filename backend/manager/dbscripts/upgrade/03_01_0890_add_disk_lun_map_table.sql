CREATE TABLE disk_lun_map (
    disk_id UUID CONSTRAINT disk_lun_to_disk_fk REFERENCES base_disks(disk_id),
    lun_id VARCHAR CONSTRAINT disk_lun_to_lun_fk REFERENCES luns(lun_id),
    CONSTRAINT disk_lun_map_pk PRIMARY KEY (disk_id, lun_id)
);

