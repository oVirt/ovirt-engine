---------------------------------------------------------------------
--  table cpu_profiles
---------------------------------------------------------------------
CREATE TABLE cpu_profiles
(
  id UUID PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  cluster_id UUID NOT NULL,
  qos_id UUID,
  description TEXT,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE,
  FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL
) WITH OIDS;

DROP INDEX IF EXISTS IDX_cpu_profiles_cluster_id;
CREATE INDEX IDX_cpu_profiles_cluster_id ON cpu_profiles(cluster_id);

