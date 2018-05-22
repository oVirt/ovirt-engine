ALTER TABLE ad_groups
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN domain SET NOT NULL,
    ALTER COLUMN distinguishedname SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN surname SET NOT NULL,
    ALTER COLUMN department SET NOT NULL;

ALTER TABLE audit_log
    ALTER COLUMN user_name SET NOT NULL,
    ALTER COLUMN vm_name SET NOT NULL,
    ALTER COLUMN vm_template_name SET NOT NULL,
    ALTER COLUMN vds_name SET NOT NULL,
    ALTER COLUMN storage_pool_name SET NOT NULL,
    ALTER COLUMN storage_domain_name SET NOT NULL,
    ALTER COLUMN cluster_name SET NOT NULL,
    ALTER COLUMN quota_name SET NOT NULL,
    ALTER COLUMN gluster_volume_name SET NOT NULL;

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
