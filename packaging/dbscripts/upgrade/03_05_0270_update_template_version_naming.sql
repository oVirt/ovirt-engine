-- copy name to version_name where version_name is empty
update vm_static
set template_version_name=vm_name
where entity_type='TEMPLATE' and template_version_number>1 and
(template_version_name is null or template_version_name='');

-- copy base name to name for versions
update vm_static vm1
set vm_name=
  (select vm2.vm_name from vm_static vm2
   where vm2.vm_guid = vm1.vmt_guid)
where entity_type='TEMPLATE' and template_version_number>1;
