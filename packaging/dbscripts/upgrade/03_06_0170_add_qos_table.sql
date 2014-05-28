-- ----------------------------------------------------------------------
--  table qos
-- ----------------------------------------------------------------------

CREATE TABLE qos
(
  id uuid NOT NULL,
  qos_type SMALLINT NOT NULL,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  storage_pool_id uuid NOT NULL,
  max_throughput INTEGER,
  max_read_throughput INTEGER,
  max_write_throughput INTEGER,
  max_iops INTEGER,
  max_read_iops INTEGER,
  max_write_iops INTEGER,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE default NULL,
  CONSTRAINT PK_qos_id PRIMARY KEY (id)
) WITH OIDS;

ALTER TABLE qos ADD CONSTRAINT fk_qos_storage_pool FOREIGN KEY (storage_pool_id)
      REFERENCES storage_pool (id)
      ON UPDATE NO ACTION ON DELETE CASCADE;

-- add index on storage_pool_id
CREATE INDEX IDX_qos_storage_pool_id ON qos (storage_pool_id);

