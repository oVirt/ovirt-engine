update roles set name ='UserRole', description = 'Standard User Role'
where id = '00000000-0000-0000-0001-000000000001';

update roles set name ='PowerUserRole', description = 'User Role, allowed to create/manage Vms and Templates'
where id = '00000000-0000-0000-0001-000000000002';

update roles set name ='UserVmManager', description = 'User Role, with permission for any operation on Vms'
where id = 'DEF00006-0000-0000-0000-DEF000000006';

update roles set description = 'Administrator Role, permission for all operations on a specific Template'
where id = 'DEF00008-0000-0000-0000-DEF000000008';

update roles set name ='UserTemplateBasedVm', description = 'User Role, with permissions only to use Templates'
where id = 'DEF00009-0000-0000-0000-DEF000000009';

update roles set description = 'System Administrators with permission for all operations'
where id = '00000000-0000-0000-0000-000000000001';

update roles set description = 'Administrator Role, permission for all the objects underneath a specific Cluster'
where id = 'DEF00001-0000-0000-0000-DEF000000001';

update roles set description = 'Administrator Role, permission for all the objects underneath a specific Data Center, except Storage'
where id = 'DEF00002-0000-0000-0000-DEF000000002';

update roles set description = 'Administrator Role, permission for all operations on a specific Storage Domain'
where id = 'DEF00003-0000-0000-0000-DEF000000003';

update roles set description = 'Administrator Role, permission for all operations on a specific Host'
where id = 'DEF00004-0000-0000-0000-DEF000000004';

update roles set description = 'Administrator Role, permission for all operations on a specific Logical Network'
where id = 'DEF00005-0000-0000-0000-DEF000000005';

update roles set description = 'Administrator Role, permission for all operations on a specific VM Pool'
where id = 'DEF00007-0000-0000-0000-DEF000000007';

