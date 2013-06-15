SELECT fn_db_change_column_type('quota','quota_name','varchar','varchar (65)');

-- this second part of the script is commented due to
-- the new quota design there is no need for default quota.

-- UPDATE quota
-- SET    quota_name = 'DefaultQuota-' || (SELECT name FROM storage_pool WHERE storage_pool.id = quota.storage_pool_id)
-- WHERE  is_default_quota;
