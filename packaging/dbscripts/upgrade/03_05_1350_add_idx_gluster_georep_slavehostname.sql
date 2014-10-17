DROP INDEX if exists IDX_georep_slave_host_name;
CREATE INDEX IDX_georep_slave_host_name ON gluster_georep_session(slave_host_name);
