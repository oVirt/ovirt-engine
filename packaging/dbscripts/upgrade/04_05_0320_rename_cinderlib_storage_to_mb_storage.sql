ALTER TABLE cinder_storage RENAME TO mb_storage;
ALTER INDEX pk_cinder_storage RENAME TO pk_mb_storage;
DROP FUNCTION IF EXISTS InsertCinderStorage(v_storage_domain_id UUID, v_driver_options JSONB, v_driver_sensitive_options TEXT);
DROP FUNCTION IF EXISTS UpdateCinderStorage(v_storage_domain_id UUID, v_driver_options JSONB, v_driver_sensitive_options TEXT);
DROP FUNCTION IF EXISTS DeleteCinderStorage(v_storage_domain_id UUID);
DROP FUNCTION IF EXISTS GetCinderStorage(v_storage_domain_id UUID);
DROP FUNCTION IF EXISTS GetCinderStorageByDrivers(v_driver_options JSONB);
