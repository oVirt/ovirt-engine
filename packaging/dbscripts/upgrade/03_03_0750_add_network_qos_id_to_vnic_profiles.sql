select fn_db_add_column('vnic_profiles', 'network_qos_id', 'UUID');
DROP INDEX IF EXISTS IDX_vnic_profiles_network_qos_id;
CREATE INDEX IDX_vnic_profiles_network_qos_id ON vnic_profiles(network_qos_id);
select fn_db_create_constraint('vnic_profiles', 'FK_vnic_profiles_network_qos_id', 'FOREIGN KEY (network_qos_id) REFERENCES network_qos(id) ON DELETE SET NULL');
