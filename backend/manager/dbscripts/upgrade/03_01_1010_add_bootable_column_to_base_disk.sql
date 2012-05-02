select fn_db_add_column('base_disks', 'boot', 'boolean NOT NULL DEFAULT false');

CREATE OR REPLACE FUNCTION base_disks_upgrade_boolean_03_01_1010()
RETURNS void
AS $function$
BEGIN
    INSERT INTO base_disks (boot)
    SELECT boot from images
    WHERE images.image_group_id = base_disks.id;
END; $function$
LANGUAGE plpgsql;

select base_disks_upgrade_boolean_03_01_1010();

DROP FUNCTION base_disks_upgrade_boolean_03_01_1010();

select fn_db_drop_column('images','boot');

