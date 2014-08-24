-- add network qos columns to qos table
SELECT fn_db_add_column('qos', 'inbound_average', 'SMALLINT DEFAULT NULL');
SELECT fn_db_add_column('qos', 'inbound_peak', 'SMALLINT DEFAULT NULL');
SELECT fn_db_add_column('qos', 'inbound_burst', 'SMALLINT DEFAULT NULL');
SELECT fn_db_add_column('qos', 'outbound_average', 'SMALLINT DEFAULT NULL');
SELECT fn_db_add_column('qos', 'outbound_peak', 'SMALLINT DEFAULT NULL');
SELECT fn_db_add_column('qos', 'outbound_burst', 'SMALLINT DEFAULT NULL');
-- copy fields data (qos_type = 3)
INSERT INTO qos(
       id, qos_type, name, description, storage_pool_id, inbound_average, inbound_peak, inbound_burst,
       outbound_average, outbound_peak, outbound_burst, _create_date,
       _update_date)
SELECT id, 3, name, NULL, storage_pool_id, inbound_average, inbound_peak, inbound_burst,
       outbound_average, outbound_peak, outbound_burst, _create_date,
       _update_date
FROM network_qos;
-- drop old references
SELECT fn_db_drop_constraint('vnic_profiles', 'FK_vnic_profiles_network_qos_id');
SELECT fn_db_drop_constraint('network', 'fk_network_qos_id');
-- drop table
DROP TABLE network_qos;
-- create new references
SELECT fn_db_create_constraint('vnic_profiles', 'fk_vnic_profiles_network_qos_id', 'FOREIGN KEY (network_qos_id) REFERENCES qos(id) ON DELETE SET NULL');
SELECT fn_db_create_constraint('network', 'fk_network_qos_id', 'FOREIGN KEY (qos_id) REFERENCES qos(id) ON DELETE SET NULL');
--  allow for anonymous network QoS entities to be persisted, to be revisited.
ALTER TABLE qos
      ALTER COLUMN name DROP NOT NULL,
      ALTER COLUMN storage_pool_id DROP NOT NULL;


