-- Update that adjusts vm_static.default_display_type enumeration
-- according to corresponding video device of vm/template.

update vm_static
set default_display_type = case
    when device = 'vga'
        then 0
    when device = 'cirrus'
        then 1
        else 2
end
from vm_device
where vm_device.vm_id = vm_static.vm_guid and vm_device.type = 'video';
