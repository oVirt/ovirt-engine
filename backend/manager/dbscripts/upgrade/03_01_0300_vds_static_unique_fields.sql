
ALTER TABLE vds_static ADD CONSTRAINT vds_static_vds_name_unique UNIQUE(vds_name);
ALTER TABLE vds_static ADD CONSTRAINT vds_static_host_name_unique UNIQUE(host_name);

