SELECT fn_db_add_column ('vnic_profiles', 'failover_vnic_profile_id', 'UUID');

SELECT fn_db_create_constraint('vnic_profiles', 'failover_vnic_profile_id_fk', 'FOREIGN KEY(failover_vnic_profile_id) REFERENCES vnic_profiles(id) ON DELETE SET NULL');
