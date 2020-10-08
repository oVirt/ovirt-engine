SELECT fn_db_add_column('vm_static', 'effective_bios_type', 'integer not null default 0');

UPDATE vm_static
    SET effective_bios_type =
        CASE
            WHEN vm_static.cluster_id IS NULL THEN 1
            WHEN vm_static.custom_bios_type <> 0 THEN vm_static.custom_bios_type
            ELSE cluster.bios_type
        END
    FROM cluster WHERE vm_static.cluster_id IS NULL OR vm_static.cluster_id = cluster.cluster_id;
