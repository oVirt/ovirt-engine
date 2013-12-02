SELECT fn_db_add_column('network', 'qos_id', 'UUID');
SELECT fn_db_create_constraint('network', 'fk_network_qos_id', 'FOREIGN KEY (qos_id) REFERENCES network_qos(id) ON DELETE SET NULL');
