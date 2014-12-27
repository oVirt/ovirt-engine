CREATE TABLE gluster_config_master
(
    config_key VARCHAR(50),
    config_description VARCHAR(300),
    minimum_supported_cluster VARCHAR(50),
    config_possible_values VARCHAR(50),
    config_feature VARCHAR(50),
    CONSTRAINT pk_config_key PRIMARY KEY(config_key)
) WITH OIDS;

ALTER TABLE ONLY gluster_georep_config
    ADD CONSTRAINT fk_config_key FOREIGN KEY(config_key) REFERENCES gluster_config_master(config_key);
