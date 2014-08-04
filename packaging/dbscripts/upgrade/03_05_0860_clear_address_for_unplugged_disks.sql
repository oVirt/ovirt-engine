UPDATE vm_device SET address = ''
WHERE is_managed AND device = 'disk' AND NOT is_plugged;