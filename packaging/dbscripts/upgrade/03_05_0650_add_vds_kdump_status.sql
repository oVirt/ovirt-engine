CREATE TABLE vds_kdump_status (
    vds_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    CONSTRAINT pk_vds_kdump_status PRIMARY KEY(vds_id),
    CONSTRAINT fk_vds_kdump_status_vds_static
        FOREIGN KEY (vds_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE
) WITH OIDS;

CREATE INDEX idx_vds_kdump_status_status ON vds_kdump_status(status);
