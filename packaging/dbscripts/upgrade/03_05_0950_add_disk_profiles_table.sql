---------------------------------------------------------------------
--  table disk_profiles
---------------------------------------------------------------------
CREATE TABLE disk_profiles
(
  id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  storage_domain_id UUID NOT NULL,
  qos_id UUID,
  description TEXT,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (storage_domain_id) REFERENCES storage_domain_static(id) ON DELETE CASCADE,
  FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL
) WITH OIDS;

DROP INDEX IF EXISTS IDX_disk_profiles_storage_domain_id;
CREATE INDEX IDX_disk_profiles_storage_domain_id ON disk_profiles(storage_domain_id);

