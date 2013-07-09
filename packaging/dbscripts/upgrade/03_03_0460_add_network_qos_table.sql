-- ----------------------------------------------------------------------
--  table network_qos
-- ----------------------------------------------------------------------

CREATE TABLE network_qos
(
  id uuid NOT NULL,
  name VARCHAR(50) NOT NULL,
  storage_pool_id uuid NOT NULL,
  inbound_average INTEGER,
  inbound_peak INTEGER,
  inbound_burst INTEGER,
  outbound_average INTEGER,
  outbound_peak INTEGER,
  outbound_burst INTEGER,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE default NULL,
  CONSTRAINT PK_network_qos_id PRIMARY KEY (id)
) WITH OIDS;

ALTER TABLE network_qos ADD CONSTRAINT network_qos_storage_pool_fk
FOREIGN KEY(storage_pool_id) REFERENCES storage_pool(id) ON DELETE CASCADE;

-- add index on storage_pool_id
CREATE INDEX IDX_network_qos_storage_pool_id ON network_qos
(storage_pool_id);