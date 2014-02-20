CREATE TABLE storage_domains_ovf_info
(
   storage_domain_id UUID REFERENCES storage_domain_static(id) ON DELETE CASCADE,
   status INTEGER DEFAULT 0,
   ovf_disk_id UUID PRIMARY KEY REFERENCES base_disks(disk_id) ON DELETE CASCADE,
   stored_ovfs_ids TEXT,
   last_updated TIMESTAMP WITH TIME ZONE
);


