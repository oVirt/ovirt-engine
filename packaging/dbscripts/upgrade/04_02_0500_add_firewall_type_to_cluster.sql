SELECT fn_db_add_column('cluster', 'firewall_type', 'INT NOT NULL DEFAULT 1');
UPDATE cluster SET firewall_type = 0;
