CREATE TABLE gluster_global_volume_options (
    id UUID NOT NULL,
    cluster_id UUID NOT NULL,
    option_key VARCHAR(8192) NOT NULL,
    option_val VARCHAR(8192) NOT NULL
);

ALTER TABLE ONLY gluster_global_volume_options
    ADD CONSTRAINT pk_gluster_global_volume_options PRIMARY KEY (id);