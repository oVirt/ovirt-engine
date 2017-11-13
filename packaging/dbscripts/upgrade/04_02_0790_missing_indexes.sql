-- Following the report of pgcluu analysis tool we are missing
-- indexes for handful of columns. Our schema isn't built for mass records
-- and the expected cost of more indexes in disk space is negligible.
CREATE INDEX IF NOT EXISTS idx_storage_domain_dr_georep_session_id ON storage_domain_dr (georep_session_id);
CREATE INDEX IF NOT EXISTS idx_vds_static_openstack_network_provider_id ON vds_static (openstack_network_provider_id);
CREATE INDEX IF NOT EXISTS idx_disk_vm_element_disk_id ON disk_vm_element (disk_id);
CREATE INDEX IF NOT EXISTS idx_qrtz_triggers_job_group_sched_name_job_name ON qrtz_triggers (job_group,sched_name,job_name);
CREATE INDEX IF NOT EXISTS idx_network_dns_resolver_configuration_id ON network (dns_resolver_configuration_id);
CREATE INDEX IF NOT EXISTS idx_unregistered_disks_storage_domain_id ON unregistered_disks (storage_domain_id);
CREATE INDEX IF NOT EXISTS idx_vnic_profiles_network_filter_id ON vnic_profiles (network_filter_id);
CREATE INDEX IF NOT EXISTS idx_network_attachments_dns_resolver_configuration_id ON network_attachments (dns_resolver_configuration_id);
CREATE INDEX IF NOT EXISTS idx_cluster_mac_pool_id ON cluster (mac_pool_id);
CREATE INDEX IF NOT EXISTS idx_qrtz_blob_triggers_trigger_name_sched_name_trigger_group ON qrtz_blob_triggers (trigger_name,sched_name,trigger_group);
CREATE INDEX IF NOT EXISTS idx_vds_dynamic_dns_resolver_configuration_id ON vds_dynamic (dns_resolver_configuration_id);
CREATE INDEX IF NOT EXISTS idx_host_device_physfn_host_id ON host_device (physfn,host_id);
CREATE INDEX IF NOT EXISTS idx_cluster_default_network_provider_id ON cluster (default_network_provider_id);
CREATE INDEX IF NOT EXISTS idx_qrtz_simple_triggers_trigger_group_sched_name_trigger_name ON qrtz_simple_triggers (trigger_group,sched_name,trigger_name);
CREATE INDEX IF NOT EXISTS idx_storage_domain_dr_storage_domain_id ON storage_domain_dr (storage_domain_id);
CREATE INDEX IF NOT EXISTS idx_disk_vm_element_vm_id ON disk_vm_element (vm_id);
CREATE INDEX IF NOT EXISTS idx_qrtz_simprop_triggers_trigger_name_trigger_group_sched_name ON qrtz_simprop_triggers (trigger_name,trigger_group,sched_name);
CREATE INDEX IF NOT EXISTS idx_storage_server_connections_gluster_volume_id ON storage_server_connections (gluster_volume_id);
CREATE INDEX IF NOT EXISTS idx_vm_static_lease_sd_id ON vm_static (lease_sd_id);
CREATE INDEX IF NOT EXISTS idx_name_server_dns_resolver_configuration_id ON name_server (dns_resolver_configuration_id);
