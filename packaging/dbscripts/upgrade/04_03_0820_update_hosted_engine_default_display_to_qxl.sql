-- Change HostedEngine vm Default Display Type to QXL (1)
UPDATE vm_static
SET default_display_type = 1
WHERE origin = 5 OR origin = 6;