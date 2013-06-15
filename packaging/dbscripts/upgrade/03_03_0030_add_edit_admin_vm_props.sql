-- assign EDIT_ADMIN_VM_PROPERTIES action to selected administrator roles
INSERT INTO roles_groups(role_id,action_group_id)
select
    id,
    15 -- EDIT_ADMIN_VM_PROPERTIES
from roles
where
    name in ('SuperUser', 'ClusterAdmin', 'DataCenterAdmin')
    and id not in (
       select role_id from roles_groups rg
       where
           rg.role_id in (select id from roles where name in ('SuperUser', 'ClusterAdmin', 'DataCenterAdmin')
           and rg.action_group_id = 15));

-- assign EDIT_ADMIN_TEMPLATE_PROPERTIES action to selected administrator roles
INSERT INTO roles_groups(role_id,action_group_id)
select
    id,
    205 -- EDIT_ADMIN_TEMPLATE_PROPERTIES
from roles
where
    name in ('SuperUser', 'DataCenterAdmin', 'TemplateAdmin')
    and id not in (
       select role_id from roles_groups rg
       where
           rg.role_id in (select id from roles where name in ('SuperUser', 'DataCenterAdmin', 'TemplateAdmin')
           and rg.action_group_id = 205));
