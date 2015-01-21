-- change preparing-to-hibernate status to up
UPDATE vm_dynamic set status = 1 where status = 17;
