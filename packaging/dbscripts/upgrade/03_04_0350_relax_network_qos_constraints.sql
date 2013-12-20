ALTER TABLE network_qos
      ALTER COLUMN name DROP NOT NULL,
      ALTER COLUMN storage_pool_id DROP NOT NULL;
