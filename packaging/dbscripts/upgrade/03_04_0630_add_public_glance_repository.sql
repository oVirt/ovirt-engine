-- Adding the Glance provider
insert into providers(id, name, description, url, provider_type, auth_required) values ('ceab03af-7220-4d42-8f5c-9b557f5d29af', 'ovirt-image-repository', 'Public Glance repository for oVirt', 'http://glance.ovirt.org:9292', 'OPENSTACK_IMAGE', false);

-- Adding a proper storage domain static entry
insert into storage_domain_static(id, storage, storage_name, storage_domain_type, storage_type, storage_domain_format_type, recoverable) values ('072fbaa1-08f3-4a40-9f34-a5ca22dd1d74', 'ceab03af-7220-4d42-8f5c-9b557f5d29af', 'ovirt-image-repository', 4, 8, 0, true);

-- Adding a proper storage domain dynamic entry
insert into storage_domain_dynamic(id, available_disk_size, used_disk_size) values ('072fbaa1-08f3-4a40-9f34-a5ca22dd1d74', 0, 0);

