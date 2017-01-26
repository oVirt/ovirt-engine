UPDATE images
SET qcow_compat = 1
WHERE volume_format = 4
AND qcow_compat = 0;

