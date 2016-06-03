SELECT fn_db_add_column('cluster',
                        'mac_pool_id',
                        'UUID REFERENCES mac_pools (id)');

UPDATE
  cluster AS c
SET
  mac_pool_id = (
      SELECT
        sp.mac_pool_id
      FROM
        storage_pool sp
      WHERE
        sp.id = c.storage_pool_id
    );

ALTER TABLE cluster ALTER COLUMN mac_pool_id SET NOT NULL;
