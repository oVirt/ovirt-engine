DO $$
  DECLARE
    sp_id UUID;
    new_guid UUID;
    rand_num INTEGER;
  BEGIN
    PERFORM fn_db_add_column('quota','is_default','boolean DEFAULT false');

    PERFORM fn_db_drop_constraint('quota','quota_quota_name_unique');
    PERFORM fn_db_create_constraint('quota','quota_quota_name_unique', 'UNIQUE (storage_pool_id, quota_name)');

    FOR sp_id in (SELECT id FROM storage_pool)
    LOOP
      -- If a quota with the name 'Default' exists, rename it
      rand_num := floor(random() * 1000);
      UPDATE quota
        SET quota_name = 'Default-old-' || rand_num
        WHERE quota_name = 'Default'
              AND storage_pool_id = sp_id;

      new_guid := uuid_generate_v1();

      INSERT INTO quota (
        id,
        storage_pool_id,
        quota_name,
        description,
        is_default
      )
      VALUES (
        new_guid,
        sp_id,
        'Default',
        'Default unlimited quota',
        TRUE
      );

      INSERT INTO quota_limitation (
        id,
        quota_id,
        storage_id,
        cluster_id,
        virtual_cpu,
        mem_size_mb,
        storage_size_gb
      )
      VALUES (
        new_guid,
        new_guid,
        NULL,
        NULL,
        -1,
        -1,
        -1
      );

    END LOOP;

    CREATE UNIQUE INDEX idx_quota_storage_pool_id_default
      ON quota(storage_pool_id)
      WHERE is_default = TRUE;
  END;
$$;