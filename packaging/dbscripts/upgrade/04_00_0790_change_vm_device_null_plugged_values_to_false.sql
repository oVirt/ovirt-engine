--upgrade script : move all hosted engine disk images from unassigned status to ok
--when the vm is plugged.
--This is a fix to the state where hosted engine disk were initially set to UNASSIGNED as default
--when hosted engine was deployed and imported.
UPDATE images
SET imagestatus = 1   --OK
WHERE imagestatus = 0 --UNASSIGNED
    AND image_group_id IN
   (SELECT device_id FROM vm_device
    WHERE (vm_id IN(SELECT vm_guid FROM vm_static WHERE origin=6))
    AND is_plugged);

