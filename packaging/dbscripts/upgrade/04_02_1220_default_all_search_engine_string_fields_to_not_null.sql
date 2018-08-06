ALTER TABLE cluster
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN free_text_comment SET NOT NULL;

ALTER TABLE vm_device
    ALTER COLUMN alias SET NOT NULL;

ALTER TABLE network
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN free_text_comment SET NOT NULL;

ALTER TABLE providers
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE quota
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE storage_pool
    ALTER COLUMN free_text_comment SET NOT NULL;

ALTER TABLE vds_static
    ALTER COLUMN free_text_comment SET NOT NULL;

ALTER TABLE vm_static
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN free_text_comment SET NOT NULL;

