UPDATE vm_device
SET is_plugged = TRUE
WHERE type IN ('graphics', 'console');
