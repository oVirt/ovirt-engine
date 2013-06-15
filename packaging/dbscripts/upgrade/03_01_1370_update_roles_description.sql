update roles set description = 'User Role, permissions to consume the Quota resources' -- QuotaConsumer
where id = 'def0000a-0000-0000-0000-def00000000a';


update roles set description = 'User Role, permissions for all operations on a specific disk' -- DiskOperator
where id = 'def0000a-0000-0000-0000-def00000000b';

update roles set description = 'User Role, permission to create Disks' -- DiskCreator
where id = 'def0000a-0000-0000-0000-def00000000c';

update roles set description = 'User Role, permission to create VMs' -- VmCreator
where id = 'def0000a-0000-0000-0000-def00000000d';

update roles set description = 'User Role, permission to create Templates' -- TemplateCreator
where id = 'def0000a-0000-0000-0000-def00000000e';

update roles set description = 'User Role, permissions for all operations on Templates' -- TemplateOwner
where id = 'def0000a-0000-0000-0000-def00000000f';

update roles set description = 'Administrator Role, permissions for operations on Gluster objects' --GlusterAdmin
where id = 'def0000b-0000-0000-0000-def00000000b';

