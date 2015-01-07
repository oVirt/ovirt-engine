-- Adjust default_display_type and video card for blank template to QXL.

update vm_static set default_display_type = 2 where vm_guid = '00000000-0000-0000-0000-000000000000'; -- qxl
update vm_device set device = 'qxl' where vm_id = '00000000-0000-0000-0000-000000000000' and type = 'video';
